package com.example.sca.scan;

import com.example.sca.auth.CurrentUser;
import com.example.sca.auth.UserPrincipal;
import com.example.sca.common.ApiException;
import com.example.sca.project.ProjectDto;
import com.example.sca.project.ProjectRequest;
import com.example.sca.project.ProjectService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/container-images")
public class ContainerImageController {
    private final ProjectService projectService;
    private final ScanService scanService;

    public ContainerImageController(ProjectService projectService, ScanService scanService) {
        this.projectService = projectService;
        this.scanService = scanService;
    }

    @PostMapping("/scans")
    public ImageScanResult scan(@RequestBody ImageScanRequest request, @CurrentUser UserPrincipal user) {
        if (request.imageRef == null || request.imageRef.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Container image is required");
        }
        ProjectRequest projectRequest = new ProjectRequest();
        projectRequest.setName(request.name == null || request.name.trim().isEmpty() ? request.imageRef.trim() : request.name.trim());
        projectRequest.setGitUrl("image://" + request.imageRef.trim());
        projectRequest.setDefaultBranch("image");
        ProjectDto project = projectService.create(projectRequest, user);
        ScanTaskDto scan = scanService.createManualScan(project.getId(), user);
        ImageScanResult result = new ImageScanResult();
        result.project = project;
        result.scan = scan;
        return result;
    }

    public static class ImageScanRequest {
        public String name;
        public String imageRef;
    }

    public static class ImageScanResult {
        public ProjectDto project;
        public ScanTaskDto scan;
    }
}
