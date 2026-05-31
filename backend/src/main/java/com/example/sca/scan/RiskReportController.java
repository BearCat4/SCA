package com.example.sca.scan;

import com.example.sca.auth.CurrentUser;
import com.example.sca.auth.UserPrincipal;
import com.example.sca.project.ProjectDto;
import com.example.sca.project.ProjectService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports/risk")
public class RiskReportController {
    private final ProjectService projectService;
    private final ScanTaskRepository scanTaskRepository;

    public RiskReportController(ProjectService projectService, ScanTaskRepository scanTaskRepository) {
        this.projectService = projectService;
        this.scanTaskRepository = scanTaskRepository;
    }

    @GetMapping
    public RiskReportDto get(@CurrentUser UserPrincipal user) {
        RiskReportDto dto = new RiskReportDto();
        dto.generatedAt = Instant.now();
        for (ProjectDto project : projectService.list(user)) {
            ProjectRisk row = new ProjectRisk();
            row.projectId = project.getId();
            row.projectName = project.getName();
            scanTaskRepository.findTopByProjectIdOrderByIdDesc(project.getId()).ifPresent(task -> {
                row.scanId = task.getId();
                row.status = task.getStatus();
                row.vulnerabilities = task.getVulnerabilityCount();
                row.critical = task.getCriticalCount();
                row.high = task.getHighCount();
                row.medium = task.getMediumCount();
                row.low = task.getLowCount();
                row.components = task.getComponentCount();
                row.licenses = task.getLicenseCount();
                row.riskScore = score(row.critical, row.high, row.medium, row.low);
                dto.vulnerabilities += task.getVulnerabilityCount();
                dto.critical += task.getCriticalCount();
                dto.high += task.getHighCount();
                dto.medium += task.getMediumCount();
                dto.low += task.getLowCount();
                dto.components += task.getComponentCount();
                dto.licenses += task.getLicenseCount();
                dto.riskScore += row.riskScore;
                if (task.getStatus() == ScanStatus.FAILED) {
                    dto.failedProjects += 1;
                }
            });
            if (row.scanId == null) {
                dto.unscannedProjects += 1;
            }
            dto.projects.add(row);
        }
        dto.projects.sort(Comparator.comparingInt((ProjectRisk row) -> row.riskScore).reversed());
        dto.projectCount = dto.projects.size();
        return dto;
    }

    private int score(int critical, int high, int medium, int low) {
        return critical * 10 + high * 5 + medium * 2 + low;
    }

    public static class RiskReportDto {
        public Instant generatedAt;
        public int projectCount;
        public int unscannedProjects;
        public int failedProjects;
        public int components;
        public int vulnerabilities;
        public int critical;
        public int high;
        public int medium;
        public int low;
        public int licenses;
        public int riskScore;
        public List<ProjectRisk> projects = new ArrayList<ProjectRisk>();
    }

    public static class ProjectRisk {
        public Long projectId;
        public String projectName;
        public Long scanId;
        public ScanStatus status;
        public int components;
        public int vulnerabilities;
        public int critical;
        public int high;
        public int medium;
        public int low;
        public int licenses;
        public int riskScore;
    }
}
