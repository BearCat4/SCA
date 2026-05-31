package com.example.sca.project;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sca.auth.AppUser;
import com.example.sca.auth.TokenService;
import com.example.sca.auth.UserPrincipal;
import com.example.sca.auth.UserRepository;
import com.example.sca.auth.UserRole;
import com.example.sca.common.Severity;
import com.example.sca.scan.ComponentFinding;
import com.example.sca.scan.ComponentRepository;
import com.example.sca.scan.ScanStatus;
import com.example.sca.scan.ScanTask;
import com.example.sca.scan.ScanTaskRepository;
import com.example.sca.scan.ScanTrigger;
import com.example.sca.scan.Vulnerability;
import com.example.sca.scan.VulnerabilityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({ProjectService.class, TokenService.class})
class ProjectServiceManagementTest {
    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ScanTaskRepository scanTaskRepository;

    @Autowired
    private VulnerabilityRepository vulnerabilityRepository;

    @Autowired
    private ComponentRepository componentRepository;

    @Test
    void deleteRemovesProjectAndScanAssets() {
        AppUser user = user("owner", UserRole.ADMIN);
        Project project = project(user);
        ScanTask scanTask = scanTask(project);
        vulnerabilityRepository.save(vulnerability(scanTask));
        componentRepository.save(component(scanTask));

        projectService.delete(project.getId(), principal(user));

        assertThat(projectRepository.findById(project.getId())).isEmpty();
        assertThat(scanTaskRepository.findByProjectIdOrderByIdDesc(project.getId())).isEmpty();
        assertThat(vulnerabilityRepository.findAll()).isEmpty();
        assertThat(componentRepository.findAll()).isEmpty();
    }

    private AppUser user(String username, UserRole role) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPasswordHash("hash");
        user.setRole(role);
        return userRepository.save(user);
    }

    private UserPrincipal principal(AppUser user) {
        return new UserPrincipal(user.getId(), user.getUsername(), user.getRole());
    }

    private Project project(AppUser user) {
        Project project = new Project();
        project.setName("demo");
        project.setGitUrl("https://example.test/demo.git");
        project.setDefaultBranch("main");
        project.setOwner(user);
        project.setTokenHash("token-hash");
        return projectRepository.save(project);
    }

    private ScanTask scanTask(Project project) {
        ScanTask task = new ScanTask();
        task.setProject(project);
        task.setBranch("main");
        task.setTriggerType(ScanTrigger.MANUAL);
        task.setStatus(ScanStatus.PASSED);
        return scanTaskRepository.save(task);
    }

    private Vulnerability vulnerability(ScanTask task) {
        Vulnerability vulnerability = new Vulnerability();
        vulnerability.setScanTask(task);
        vulnerability.setVulnerabilityId("CVE-2026-0001");
        vulnerability.setPackageName("demo");
        vulnerability.setSeverity(Severity.HIGH);
        return vulnerability;
    }

    private ComponentFinding component(ScanTask task) {
        ComponentFinding component = new ComponentFinding();
        component.setScanTask(task);
        component.setPackageName("demo");
        component.setVersion("1.0.0");
        return component;
    }
}
