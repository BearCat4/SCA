package com.example.sca.scan;

import com.example.sca.common.ApiException;
import com.example.sca.project.Project;
import com.example.sca.project.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ci/projects")
public class CiScanController {
    private final ProjectService projectService;
    private final ScanService scanService;

    public CiScanController(ProjectService projectService, ScanService scanService) {
        this.projectService = projectService;
        this.scanService = scanService;
    }

    @PostMapping("/{projectId}/scans")
    public ScanTaskDto create(@PathVariable Long projectId,
                              @RequestBody(required = false) ScanRequest request,
                              @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Project token is required");
        }
        Project project = projectService.findByToken(authorization.substring(7));
        if (!project.getId().equals(projectId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Project token does not match project");
        }
        String branch = request == null ? null : request.getBranch();
        return scanService.createCiScan(project, branch);
    }
}
