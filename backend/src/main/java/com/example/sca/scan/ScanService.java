package com.example.sca.scan;

import com.example.sca.auth.UserPrincipal;
import com.example.sca.common.ApiException;
import com.example.sca.common.Severity;
import com.example.sca.config.ScanProperties;
import com.example.sca.project.Project;
import com.example.sca.project.ProjectService;
import com.example.sca.trivy.*;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ScanService {
    private static final Logger logger = LoggerFactory.getLogger(ScanService.class);
    private final ProjectService projectService;
    private final ScanTaskRepository scanTaskRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final ComponentRepository componentRepository;
    private final LicenseRepository licenseRepository;
    private final DependencyEdgeRepository dependencyEdgeRepository;
    private final GitCheckoutService gitCheckoutService;
    private final SourcePathResolver sourcePathResolver;
    private final TrivyScanner trivyScanner;
    private final TrivyReportParser trivyReportParser;
    private final DependencyTreeScanner dependencyTreeScanner;
    private final PolicyService policyService;
    private final ScanProperties scanProperties;
    private final TaskExecutor taskExecutor;
    private final TransactionTemplate transactionTemplate;

    public ScanService(ProjectService projectService,
                       ScanTaskRepository scanTaskRepository,
                       VulnerabilityRepository vulnerabilityRepository,
                       ComponentRepository componentRepository,
                       LicenseRepository licenseRepository,
                       DependencyEdgeRepository dependencyEdgeRepository,
                       GitCheckoutService gitCheckoutService,
                       SourcePathResolver sourcePathResolver,
                       TrivyScanner trivyScanner,
                       TrivyReportParser trivyReportParser,
                       DependencyTreeScanner dependencyTreeScanner,
                       PolicyService policyService,
                       ScanProperties scanProperties,
                       TaskExecutor taskExecutor,
                       TransactionTemplate transactionTemplate) {
        this.projectService = projectService;
        this.scanTaskRepository = scanTaskRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.componentRepository = componentRepository;
        this.licenseRepository = licenseRepository;
        this.dependencyEdgeRepository = dependencyEdgeRepository;
        this.gitCheckoutService = gitCheckoutService;
        this.sourcePathResolver = sourcePathResolver;
        this.trivyScanner = trivyScanner;
        this.trivyReportParser = trivyReportParser;
        this.dependencyTreeScanner = dependencyTreeScanner;
        this.policyService = policyService;
        this.scanProperties = scanProperties;
        this.taskExecutor = taskExecutor;
        this.transactionTemplate = transactionTemplate;
    }

    @Transactional
    public ScanTaskDto createManualScan(Long projectId, UserPrincipal user) {
        Project project = projectService.getAccessible(projectId, user);
        return createScan(project, project.getDefaultBranch(), ScanTrigger.MANUAL);
    }

    @Transactional
    public ScanTaskDto createCiScan(Project project, String branch) {
        return createScan(project, branch == null || branch.trim().isEmpty() ? project.getDefaultBranch() : branch.trim(), ScanTrigger.CI);
    }

    private ScanTaskDto createScan(Project project, String branch, ScanTrigger trigger) {
        ScanTask task = new ScanTask();
        task.setProject(project);
        task.setBranch(branch);
        task.setTriggerType(trigger);
        task.setStatus(ScanStatus.PENDING);
        ScanTask saved = scanTaskRepository.save(task);
        scheduleAfterCommit(saved.getId());
        return ScanTaskDto.from(saved);
    }

    private void scheduleAfterCommit(Long taskId) {
        Runnable runner = new Runnable() {
            @Override
            public void run() {
                execute(taskId);
            }
        };
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    taskExecutor.execute(runner);
                }
            });
        } else {
            taskExecutor.execute(runner);
        }
    }

    public void execute(Long taskId) {
        ScanExecutionContext context = transactionTemplate.execute(status -> markRunning(taskId));
        Path workDir = Paths.get(scanProperties.getWorkDir(), "scan-" + taskId + "-" + System.currentTimeMillis());
        Path scanPath = null;
        boolean deleteWorkDir = false;
        try {
            String rawJson;
            if (context.getGitUrl() != null && context.getGitUrl().startsWith("image://")) {
                rawJson = trivyScanner.scanImage(context.getGitUrl().substring("image://".length()));
            } else {
                scanPath = sourcePathResolver.localDirectoryOrNull(context.getGitUrl());
                if (scanPath == null) {
                    Files.createDirectories(workDir.getParent());
                    gitCheckoutService.checkout(context.getGitUrl(), context.getBranch(), workDir);
                    scanPath = workDir;
                    deleteWorkDir = true;
                }
                rawJson = trivyScanner.scan(scanPath);
            }
            ParsedTrivyReport report = trivyReportParser.parse(rawJson);
            List<DependencyEdge> dependencyEdges = scanPath == null ? Collections.emptyList() : collectDependencyEdges(scanPath);
            transactionTemplate.executeWithoutResult(status -> persistResults(taskId, rawJson, report, dependencyEdges));
        } catch (Exception ex) {
            transactionTemplate.executeWithoutResult(status -> markError(taskId, ex));
        } finally {
            if (deleteWorkDir) {
                deleteQuietly(workDir);
            }
        }
    }

    private ScanExecutionContext markRunning(Long taskId) {
        ScanTask task = scanTaskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Scan task not found"));
        task.setStatus(ScanStatus.RUNNING);
        task.setStartedAt(Instant.now());
        task.setFailureReason(null);
        scanTaskRepository.save(task);
        return new ScanExecutionContext(task.getProject().getGitUrl(), task.getBranch());
    }

    private void markError(Long taskId, Exception ex) {
        ScanTask task = scanTaskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Scan task not found"));
        task.setStatus(ScanStatus.ERROR);
        task.setFailureReason(trim(ex.getMessage()));
        task.setFinishedAt(Instant.now());
        scanTaskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public ScanTask getAccessibleScan(Long id, UserPrincipal user) {
        ScanTask task = scanTaskRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Scan task not found"));
        projectService.getAccessible(task.getProject().getId(), user);
        return task;
    }

    @Transactional(readOnly = true)
    public ScanTaskDto get(Long id, UserPrincipal user) {
        return ScanTaskDto.from(getAccessibleScan(id, user));
    }

    @Transactional(readOnly = true)
    public List<ScanTask> listByProject(Long projectId, UserPrincipal user) {
        projectService.getAccessible(projectId, user);
        return scanTaskRepository.findByProjectIdOrderByIdDesc(projectId);
    }

    @Transactional(readOnly = true)
    public Page<Vulnerability> vulnerabilities(Long id, UserPrincipal user, Pageable pageable) {
        return vulnerabilities(id, user, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Vulnerability> vulnerabilities(Long id, UserPrincipal user, String query, Severity severity, Pageable pageable) {
        return vulnerabilities(id, user, query, severity, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Vulnerability> vulnerabilities(Long id, UserPrincipal user, String query, Severity severity, VulnerabilityStatus findingStatus, Pageable pageable) {
        getAccessibleScan(id, user);
        return vulnerabilityRepository.searchByScanTaskId(id, blankToNull(query), severity, findingStatus, pageable);
    }

    @Transactional
    public FindingDtos.VulnerabilityDto updateVulnerabilityStatus(Long scanId, Long vulnerabilityId, UserPrincipal user, VulnerabilityStatus status) {
        getAccessibleScan(scanId, user);
        if (status == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Vulnerability status is required");
        }
        Vulnerability vulnerability = vulnerabilityRepository.findByIdAndScanTaskId(vulnerabilityId, scanId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Vulnerability not found"));
        vulnerability.setStatus(status);
        return FindingDtos.VulnerabilityDto.from(vulnerabilityRepository.save(vulnerability));
    }

    @Transactional(readOnly = true)
    public Page<ComponentFinding> components(Long id, UserPrincipal user, Pageable pageable) {
        return components(id, user, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<ComponentFinding> components(Long id, UserPrincipal user, String query, Pageable pageable) {
        getAccessibleScan(id, user);
        return componentRepository.searchByScanTaskId(id, blankToNull(query), pageable);
    }

    @Transactional(readOnly = true)
    public List<LicenseFinding> licenses(Long id, UserPrincipal user) {
        getAccessibleScan(id, user);
        return licenseRepository.findByScanTaskId(id);
    }

    @Transactional(readOnly = true)
    public Page<LicenseFinding> licenses(Long id, UserPrincipal user, Pageable pageable) {
        return licenses(id, user, null, null, pageable);
    }

    @Transactional(readOnly = true)
    public Page<LicenseFinding> licenses(Long id, UserPrincipal user, String query, String licenseName, Pageable pageable) {
        getAccessibleScan(id, user);
        return licenseRepository.searchByScanTaskId(id, blankToNull(query), blankToNull(licenseName), pageable);
    }

    @Transactional(readOnly = true)
    public Page<DependencyEdge> dependencies(Long id, UserPrincipal user, String query, Pageable pageable) {
        getAccessibleScan(id, user);
        return dependencyEdgeRepository.searchByScanTaskId(id, blankToNull(query), pageable);
    }

    private List<DependencyEdge> collectDependencyEdges(Path scanPath) {
        try {
            return dependencyTreeScanner.scan(scanPath);
        } catch (Exception ex) {
            logger.warn("Dependency tree collection failed; continuing scan without dependency edges", ex);
            return Collections.emptyList();
        }
    }

    private void persistResults(Long taskId, String rawJson, ParsedTrivyReport report, List<DependencyEdge> dependencyEdges) {
        ScanTask task = scanTaskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Scan task not found"));
        vulnerabilityRepository.deleteByScanTaskId(task.getId());
        componentRepository.deleteByScanTaskId(task.getId());
        licenseRepository.deleteByScanTaskId(task.getId());
        dependencyEdgeRepository.deleteByScanTaskId(task.getId());

        for (Vulnerability vulnerability : report.getVulnerabilities()) {
            vulnerability.setScanTask(task);
        }
        for (ComponentFinding component : report.getComponents()) {
            component.setScanTask(task);
        }
        for (LicenseFinding license : report.getLicenses()) {
            license.setScanTask(task);
        }
        for (DependencyEdge dependencyEdge : dependencyEdges) {
            dependencyEdge.setScanTask(task);
        }
        vulnerabilityRepository.saveAll(report.getVulnerabilities());
        componentRepository.saveAll(report.getComponents());
        licenseRepository.saveAll(report.getLicenses());
        dependencyEdgeRepository.saveAll(dependencyEdges);

        task.setRawJson(rawJson);
        task.setVulnerabilityCount(report.getVulnerabilities().size());
        task.setComponentCount(report.getComponents().size());
        task.setLicenseCount(report.getLicenses().size());
        task.setCriticalCount(count(report.getVulnerabilities(), Severity.CRITICAL));
        task.setHighCount(count(report.getVulnerabilities(), Severity.HIGH));
        task.setMediumCount(count(report.getVulnerabilities(), Severity.MEDIUM));
        task.setLowCount(count(report.getVulnerabilities(), Severity.LOW));
        task.setStatus(policyService.decideStatus(report.getVulnerabilities()));
        task.setFinishedAt(Instant.now());
        scanTaskRepository.save(task);
    }

    private static class ScanExecutionContext {
        private final String gitUrl;
        private final String branch;

        ScanExecutionContext(String gitUrl, String branch) {
            this.gitUrl = gitUrl;
            this.branch = branch;
        }

        String getGitUrl() {
            return gitUrl;
        }

        String getBranch() {
            return branch;
        }
    }

    private int count(List<Vulnerability> vulnerabilities, Severity severity) {
        int total = 0;
        for (Vulnerability vulnerability : vulnerabilities) {
            if (severity == vulnerability.getSeverity()) {
                total++;
            }
        }
        return total;
    }

    private String trim(String message) {
        if (message == null) {
            return "Scan failed";
        }
        return message.length() > 1900 ? message.substring(0, 1900) : message;
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private void deleteQuietly(Path path) {
        if (path == null || !Files.exists(path)) {
            return;
        }
        try {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ignored) {
        }
    }
}
