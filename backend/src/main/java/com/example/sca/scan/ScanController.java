package com.example.sca.scan;

import com.example.sca.auth.CurrentUser;
import com.example.sca.auth.UserPrincipal;
import com.example.sca.common.ApiException;
import com.example.sca.common.Severity;
import com.example.sca.report.ReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scans")
public class ScanController {
    private final ScanService scanService;
    private final ReportService reportService;

    public ScanController(ScanService scanService, ReportService reportService) {
        this.scanService = scanService;
        this.reportService = reportService;
    }

    @GetMapping("/{id}")
    public ScanTaskDto get(@PathVariable Long id, @CurrentUser UserPrincipal user) {
        return scanService.get(id, user);
    }

    @GetMapping("/{id}/vulnerabilities")
    public Page<FindingDtos.VulnerabilityDto> vulnerabilities(@PathVariable Long id,
                                                              @CurrentUser UserPrincipal user,
                                                              @RequestParam(required = false) String q,
                                                              @RequestParam(required = false) Severity severity,
                                                              @RequestParam(required = false) VulnerabilityStatus status,
                                                              Pageable pageable) {
        return scanService.vulnerabilities(id, user, q, severity, status, pageable).map(FindingDtos.VulnerabilityDto::from);
    }

    @PatchMapping("/{id}/vulnerabilities/{vulnerabilityId}/status")
    public FindingDtos.VulnerabilityDto updateVulnerabilityStatus(@PathVariable Long id,
                                                                  @PathVariable Long vulnerabilityId,
                                                                  @RequestBody VulnerabilityStatusRequest request,
                                                                  @CurrentUser UserPrincipal user) {
        return scanService.updateVulnerabilityStatus(id, vulnerabilityId, user, request.getStatus());
    }

    @GetMapping("/{id}/components")
    public Page<FindingDtos.ComponentDto> components(@PathVariable Long id,
                                                     @CurrentUser UserPrincipal user,
                                                     @RequestParam(required = false) String q,
                                                     Pageable pageable) {
        return scanService.components(id, user, q, pageable).map(FindingDtos.ComponentDto::from);
    }

    @GetMapping("/{id}/licenses")
    public Page<FindingDtos.LicenseDto> licenses(@PathVariable Long id,
                                                 @CurrentUser UserPrincipal user,
                                                 @RequestParam(required = false) String q,
                                                 @RequestParam(required = false) String license,
                                                 Pageable pageable) {
        return scanService.licenses(id, user, q, license, pageable).map(FindingDtos.LicenseDto::from);
    }

    @GetMapping("/{id}/dependencies")
    public Page<FindingDtos.DependencyDto> dependencies(@PathVariable Long id,
                                                        @CurrentUser UserPrincipal user,
                                                        @RequestParam(required = false) String q,
                                                        Pageable pageable) {
        return scanService.dependencies(id, user, q, pageable).map(FindingDtos.DependencyDto::from);
    }

    @GetMapping("/{id}/report.html")
    public ResponseEntity<String> html(@PathVariable Long id, @CurrentUser UserPrincipal user) {
        ScanTask task = scanService.getAccessibleScan(id, user);
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(reportService.html(task));
    }

    @GetMapping("/{id}/report.json")
    public ResponseEntity<String> json(@PathVariable Long id, @CurrentUser UserPrincipal user) {
        ScanTask task = scanService.getAccessibleScan(id, user);
        if (task.getRawJson() == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Raw report is not available");
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(task.getRawJson());
    }
}
