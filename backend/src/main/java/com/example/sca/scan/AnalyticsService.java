package com.example.sca.scan;

import com.example.sca.auth.UserPrincipal;
import com.example.sca.project.ProjectDto;
import com.example.sca.project.ProjectService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AnalyticsService {
    private final ProjectService projectService;
    private final ScanTaskRepository scanTaskRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final LicenseRepository licenseRepository;

    public AnalyticsService(ProjectService projectService,
                            ScanTaskRepository scanTaskRepository,
                            VulnerabilityRepository vulnerabilityRepository,
                            LicenseRepository licenseRepository) {
        this.projectService = projectService;
        this.scanTaskRepository = scanTaskRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.licenseRepository = licenseRepository;
    }

    @Transactional(readOnly = true)
    public List<AnalyticsDtos.ProjectRiskSummary> projectSummaries(UserPrincipal user) {
        List<ProjectDto> projects = projectService.list(user);
        List<AnalyticsDtos.ProjectRiskSummary> summaries = new ArrayList<AnalyticsDtos.ProjectRiskSummary>();
        for (ProjectDto project : projects) {
            AnalyticsDtos.ProjectRiskSummary summary = new AnalyticsDtos.ProjectRiskSummary();
            summary.projectId = project.getId();
            summary.projectName = project.getName();
            summary.source = project.getGitUrl();
            summary.defaultBranch = project.getDefaultBranch();
            Optional<ScanTask> latest = scanTaskRepository.findTopByProjectIdOrderByIdDesc(project.getId());
            if (latest.isPresent()) {
                ScanTask task = latest.get();
                summary.latestScanId = task.getId();
                summary.latestStatus = task.getStatus();
                summary.vulnerabilityCount = task.getVulnerabilityCount();
                summary.criticalCount = task.getCriticalCount();
                summary.highCount = task.getHighCount();
                summary.mediumCount = task.getMediumCount();
                summary.lowCount = task.getLowCount();
                summary.sbomCount = task.getComponentCount();
                summary.licenseCount = task.getLicenseCount();
            }
            summaries.add(summary);
        }
        return summaries;
    }

    @Transactional(readOnly = true)
    public AnalyticsDtos.CveImpactSummary cveImpact(String cve, UserPrincipal user) {
        List<Long> projectIds = projectService.list(user).stream().map(ProjectDto::getId).collect(Collectors.toList());
        AnalyticsDtos.CveImpactSummary summary = new AnalyticsDtos.CveImpactSummary();
        summary.cve = cve;
        if (projectIds.isEmpty()) {
            return summary;
        }
        summary.affectedProjectCount = vulnerabilityRepository.countAffectedProjects(cve, projectIds);
        summary.findingCount = vulnerabilityRepository.countFindings(cve, projectIds);
        List<Vulnerability> findings = vulnerabilityRepository
                .findByVulnerabilityIdIgnoreCaseAndScanTaskProjectIdIn(cve, projectIds, PageRequest.of(0, 200))
                .getContent();
        for (Vulnerability finding : findings) {
            AnalyticsDtos.CveImpactFinding dto = new AnalyticsDtos.CveImpactFinding();
            dto.projectId = finding.getScanTask().getProject().getId();
            dto.projectName = finding.getScanTask().getProject().getName();
            dto.scanId = finding.getScanTask().getId();
            dto.target = finding.getTarget();
            dto.packageName = finding.getPackageName();
            dto.installedVersion = finding.getInstalledVersion();
            dto.fixedVersion = finding.getFixedVersion();
            dto.severity = finding.getSeverity() == null ? "" : finding.getSeverity().name();
            dto.title = finding.getTitle();
            summary.findings.add(dto);
        }
        return summary;
    }

    @Transactional(readOnly = true)
    public List<AnalyticsDtos.LicenseTopItem> licenseTop(UserPrincipal user) {
        List<Long> latestScanIds = new ArrayList<Long>();
        for (ProjectDto project : projectService.list(user)) {
            scanTaskRepository.findTopByProjectIdOrderByIdDesc(project.getId())
                    .ifPresent(task -> latestScanIds.add(task.getId()));
        }
        List<AnalyticsDtos.LicenseTopItem> items = new ArrayList<AnalyticsDtos.LicenseTopItem>();
        if (latestScanIds.isEmpty()) {
            return items;
        }
        for (Object[] row : licenseRepository.countByLicenseNameInLatestScans(latestScanIds, PageRequest.of(0, 10))) {
            AnalyticsDtos.LicenseTopItem item = new AnalyticsDtos.LicenseTopItem();
            item.licenseName = String.valueOf(row[0]);
            item.count = ((Number) row[1]).longValue();
            items.add(item);
        }
        return items;
    }
}
