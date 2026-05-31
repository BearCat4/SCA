package com.example.sca.report;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.sca.common.Severity;
import com.example.sca.project.Project;
import com.example.sca.scan.ComponentRepository;
import com.example.sca.scan.DependencyEdge;
import com.example.sca.scan.DependencyEdgeRepository;
import com.example.sca.scan.LicenseRepository;
import com.example.sca.scan.ScanStatus;
import com.example.sca.scan.ScanTask;
import com.example.sca.scan.Vulnerability;
import com.example.sca.scan.VulnerabilityRepository;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

class ReportServiceTest {
    @Test
    void rendersChineseLabelsInHtmlReport() {
        VulnerabilityRepository vulnerabilityRepository = mock(VulnerabilityRepository.class);
        ComponentRepository componentRepository = mock(ComponentRepository.class);
        LicenseRepository licenseRepository = mock(LicenseRepository.class);
        DependencyEdgeRepository dependencyEdgeRepository = mock(DependencyEdgeRepository.class);
        ReportService service = new ReportService(vulnerabilityRepository, componentRepository, licenseRepository, dependencyEdgeRepository);

        ScanTask task = scanTask();
        Vulnerability vulnerability = new Vulnerability();
        vulnerability.setVulnerabilityId("CVE-1");
        vulnerability.setPackageName("demo");
        vulnerability.setInstalledVersion("1.0.0");
        vulnerability.setFixedVersion("1.0.1");
        vulnerability.setSeverity(Severity.HIGH);
        vulnerability.setTitle("demo title");
        DependencyEdge dependency = new DependencyEdge();
        dependency.setSourceName("com.example:demo");
        dependency.setSourceVersion("1.0.0");
        dependency.setTargetName("org.demo:core");
        dependency.setTargetVersion("1.1.0");
        dependency.setScope("compile");

        when(vulnerabilityRepository.findByScanTaskId(any(), any()))
                .thenReturn(new PageImpl<Vulnerability>(Collections.singletonList(vulnerability)));
        when(componentRepository.findByScanTaskId(any(), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));
        when(licenseRepository.findByScanTaskId(any()))
                .thenReturn(Collections.emptyList());
        when(dependencyEdgeRepository.searchByScanTaskId(any(), any(), any()))
                .thenReturn(new PageImpl<DependencyEdge>(Collections.singletonList(dependency)));

        String html = service.html(task);

        assertThat(html).contains("SCA 扫描报告");
        assertThat(html).contains("状态：").contains("未通过");
        assertThat(html).contains("漏洞：1 | 严重：0 | 高危：1 | 中危：0 | 低危：0");
        assertThat(html).contains("<h2>漏洞</h2>");
        assertThat(html).contains("<h2>依赖关系</h2>");
        assertThat(html).contains("<th>包</th>");
        assertThat(html).contains("<th>级别</th>");
        assertThat(html).contains("<td>高危</td>");
        assertThat(html).contains("<td>编译</td>");
        assertThat(html).doesNotContain("Status:");
        assertThat(html).doesNotContain("<th>Severity</th>");
    }

    private ScanTask scanTask() {
        Project project = new Project();
        project.setName("demo");

        ScanTask task = new ScanTask();
        task.setId(1L);
        task.setProject(project);
        task.setBranch("main");
        task.setStatus(ScanStatus.FAILED);
        task.setVulnerabilityCount(1);
        task.setHighCount(1);
        return task;
    }
}
