package com.example.sca.scan;

import com.example.sca.auth.CurrentUser;
import com.example.sca.auth.UserPrincipal;
import com.example.sca.common.ApiException;
import com.example.sca.config.ScanProperties;
import com.example.sca.project.ProjectDto;
import com.example.sca.project.ProjectRequest;
import com.example.sca.project.ProjectService;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/detection")
public class QuickDetectionController {
    private final ProjectService projectService;
    private final ScanService scanService;
    private final ScanProperties scanProperties;

    public QuickDetectionController(ProjectService projectService, ScanService scanService, ScanProperties scanProperties) {
        this.projectService = projectService;
        this.scanService = scanService;
        this.scanProperties = scanProperties;
    }

    @PostMapping("/quick")
    public QuickDetectionResult quick(@RequestBody ProjectRequest request, @CurrentUser UserPrincipal user) {
        ProjectDto project = projectService.create(request, user);
        ScanTaskDto scan = scanService.createManualScan(project.getId(), user);
        QuickDetectionResult result = new QuickDetectionResult();
        result.project = project;
        result.scan = scan;
        return result;
    }

    @PostMapping("/quick/file")
    public QuickDetectionResult file(@RequestParam("file") MultipartFile file,
                                     @RequestParam("name") String name,
                                     @CurrentUser UserPrincipal user) {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "请选择上传文件");
        }
        Path saved = saveUpload(file);
        ProjectRequest request = new ProjectRequest();
        request.setName(name);
        request.setGitUrl(saved.toString());
        request.setDefaultBranch("upload");
        return quick(request, user);
    }

    private Path saveUpload(MultipartFile file) {
        String original = file.getOriginalFilename() == null || file.getOriginalFilename().trim().isEmpty()
                ? "upload.bin" : Paths.get(file.getOriginalFilename()).getFileName().toString();
        Path uploadDir = Paths.get(scanProperties.getWorkDir(), "uploads");
        Path target = uploadDir.resolve(UUID.randomUUID().toString() + "-" + original);
        try {
            Files.createDirectories(uploadDir);
            file.transferTo(target.toFile());
            return target.toAbsolutePath().normalize();
        } catch (IOException ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "上传文件保存失败");
        }
    }

    public static class QuickDetectionResult {
        public ProjectDto project;
        public ScanTaskDto scan;
    }
}
