package com.example.sca.scan;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sca.auth.AppUser;
import com.example.sca.auth.UserRepository;
import com.example.sca.auth.UserRole;
import com.example.sca.project.Project;
import com.example.sca.project.ProjectRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class LicenseRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private ScanTaskRepository scanTaskRepository;

    @Autowired
    private LicenseRepository licenseRepository;

    @Test
    void pagesLicensesByScanTaskId() {
        ScanTask scanTask = scanTaskRepository.save(scanTask());
        licenseRepository.save(license(scanTask, "pkg-a"));
        licenseRepository.save(license(scanTask, "pkg-b"));
        licenseRepository.save(license(scanTask, "pkg-c"));

        Page<LicenseFinding> page = licenseRepository.findByScanTaskId(scanTask.getId(), PageRequest.of(1, 2));

        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(1);
        assertThat(page.getSize()).isEqualTo(2);
        assertThat(page.getContent()).extracting(LicenseFinding::getPackageName).containsExactly("pkg-c");
    }

    private ScanTask scanTask() {
        ScanTask scanTask = new ScanTask();
        scanTask.setProject(project());
        scanTask.setBranch("main");
        scanTask.setTriggerType(ScanTrigger.MANUAL);
        scanTask.setStatus(ScanStatus.PASSED);
        return scanTask;
    }

    private Project project() {
        Project project = new Project();
        project.setName("demo");
        project.setGitUrl("https://example.test/demo.git");
        project.setDefaultBranch("main");
        project.setOwner(user());
        project.setTokenHash("token-hash");
        return projectRepository.save(project);
    }

    private AppUser user() {
        AppUser user = new AppUser();
        user.setUsername("admin");
        user.setPasswordHash("hash");
        user.setRole(UserRole.ADMIN);
        return userRepository.save(user);
    }

    private LicenseFinding license(ScanTask scanTask, String packageName) {
        LicenseFinding license = new LicenseFinding();
        license.setScanTask(scanTask);
        license.setPackageName(packageName);
        license.setVersion("1.0.0");
        license.setLicenseName("Apache-2.0");
        license.setTarget("pom.xml");
        return license;
    }
}
