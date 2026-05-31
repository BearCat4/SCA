package com.example.sca.scan;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sca.auth.AppUser;
import com.example.sca.auth.UserRepository;
import com.example.sca.auth.UserRole;
import com.example.sca.common.Severity;
import com.example.sca.project.Project;
import com.example.sca.project.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class FindingRepositoryFilterTest {
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

    @Autowired
    private LicenseRepository licenseRepository;

    @Autowired
    private DependencyEdgeRepository dependencyEdgeRepository;

    private ScanTask scanTask;

    @BeforeEach
    void setUp() {
        scanTask = scanTaskRepository.save(scanTask());
    }

    @Test
    void filtersVulnerabilitiesBySearchTermAndSeverity() {
        vulnerabilityRepository.save(vulnerability("CVE-2024-0001", "org.demo:alpha", Severity.HIGH, "alpha target"));
        vulnerabilityRepository.save(vulnerability("CVE-2024-0002", "org.demo:beta", Severity.MEDIUM, "beta target"));
        vulnerabilityRepository.save(vulnerability("CVE-2024-0003", "org.demo:alpha-extra", Severity.MEDIUM, "alpha target"));

        assertThat(vulnerabilityRepository.searchByScanTaskId(scanTask.getId(), "alpha", Severity.MEDIUM, null, PageRequest.of(0, 10)).getContent())
                .extracting(Vulnerability::getVulnerabilityId)
                .containsExactly("CVE-2024-0003");
    }

    @Test
    void filtersVulnerabilitiesByTriageStatus() {
        Vulnerability open = vulnerability("CVE-2024-0100", "org.demo:open", Severity.HIGH, "open target");
        Vulnerability accepted = vulnerability("CVE-2024-0101", "org.demo:accepted", Severity.HIGH, "accepted target");
        accepted.setStatus(VulnerabilityStatus.ACCEPTED_RISK);
        vulnerabilityRepository.save(open);
        vulnerabilityRepository.save(accepted);

        assertThat(vulnerabilityRepository.searchByScanTaskId(scanTask.getId(), null, null, VulnerabilityStatus.ACCEPTED_RISK, PageRequest.of(0, 10)).getContent())
                .extracting(Vulnerability::getVulnerabilityId)
                .containsExactly("CVE-2024-0101");
    }

    @Test
    void filtersComponentsBySearchTerm() {
        componentRepository.save(component("org.demo:alpha", "service-a/pom.xml"));
        componentRepository.save(component("org.demo:beta", "service-b/pom.xml"));

        assertThat(componentRepository.searchByScanTaskId(scanTask.getId(), "service-b", PageRequest.of(0, 10)).getContent())
                .extracting(ComponentFinding::getPackageName)
                .containsExactly("org.demo:beta");
    }

    @Test
    void filtersLicensesBySearchTermAndLicenseName() {
        licenseRepository.save(license("org.demo:alpha", "Apache-2.0"));
        licenseRepository.save(license("org.demo:beta", "GPL-3.0"));
        licenseRepository.save(license("org.demo:alpha-tools", "MIT"));

        assertThat(licenseRepository.searchByScanTaskId(scanTask.getId(), "alpha", "MIT", PageRequest.of(0, 10)).getContent())
                .extracting(LicenseFinding::getPackageName)
                .containsExactly("org.demo:alpha-tools");
    }

    @Test
    void filtersDependencyEdgesBySearchTerm() {
        dependencyEdgeRepository.save(dependency("org.demo:app", "org.demo:service-a", "compile"));
        dependencyEdgeRepository.save(dependency("org.demo:app", "org.demo:service-b", "runtime"));

        assertThat(dependencyEdgeRepository.searchByScanTaskId(scanTask.getId(), "service-b", PageRequest.of(0, 10)).getContent())
                .extracting(DependencyEdge::getTargetName)
                .containsExactly("org.demo:service-b");
    }

    private ScanTask scanTask() {
        ScanTask task = new ScanTask();
        task.setProject(project());
        task.setBranch("main");
        task.setTriggerType(ScanTrigger.MANUAL);
        task.setStatus(ScanStatus.PASSED);
        return task;
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
        user.setUsername("filter-user");
        user.setPasswordHash("hash");
        user.setRole(UserRole.ADMIN);
        return userRepository.save(user);
    }

    private Vulnerability vulnerability(String cve, String packageName, Severity severity, String target) {
        Vulnerability vulnerability = new Vulnerability();
        vulnerability.setScanTask(scanTask);
        vulnerability.setVulnerabilityId(cve);
        vulnerability.setPackageName(packageName);
        vulnerability.setInstalledVersion("1.0.0");
        vulnerability.setFixedVersion("1.0.1");
        vulnerability.setSeverity(severity);
        vulnerability.setTitle(packageName + " vulnerability");
        vulnerability.setTarget(target);
        return vulnerability;
    }

    private ComponentFinding component(String packageName, String target) {
        ComponentFinding component = new ComponentFinding();
        component.setScanTask(scanTask);
        component.setPackageName(packageName);
        component.setVersion("1.0.0");
        component.setType("pom");
        component.setTarget(target);
        return component;
    }

    private LicenseFinding license(String packageName, String licenseName) {
        LicenseFinding license = new LicenseFinding();
        license.setScanTask(scanTask);
        license.setPackageName(packageName);
        license.setVersion("1.0.0");
        license.setLicenseName(licenseName);
        license.setTarget("pom.xml");
        return license;
    }

    private DependencyEdge dependency(String sourceName, String targetName, String scope) {
        DependencyEdge dependency = new DependencyEdge();
        dependency.setScanTask(scanTask);
        dependency.setSourceRef(sourceName + ":jar:1.0.0");
        dependency.setSourceName(sourceName);
        dependency.setSourceVersion("1.0.0");
        dependency.setTargetRef(targetName + ":jar:1.1.0");
        dependency.setTargetName(targetName);
        dependency.setTargetVersion("1.1.0");
        dependency.setScope(scope);
        return dependency;
    }
}
