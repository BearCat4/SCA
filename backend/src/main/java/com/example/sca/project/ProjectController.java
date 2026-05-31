package com.example.sca.project;

import com.example.sca.auth.CurrentUser;
import com.example.sca.auth.UserPrincipal;
import com.example.sca.scan.ScanService;
import com.example.sca.scan.ScanTaskDto;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final ScanService scanService;

    public ProjectController(ProjectService projectService, ScanService scanService) {
        this.projectService = projectService;
        this.scanService = scanService;
    }

    @GetMapping
    public List<ProjectDto> list(@CurrentUser UserPrincipal user) {
        return projectService.list(user);
    }

    @PostMapping
    public ProjectDto create(@RequestBody ProjectRequest request, @CurrentUser UserPrincipal user) {
        return projectService.create(request, user);
    }

    @GetMapping("/{id}")
    public ProjectDto get(@PathVariable Long id, @CurrentUser UserPrincipal user) {
        return ProjectDto.from(projectService.getAccessible(id, user));
    }

    @PutMapping("/{id}")
    public ProjectDto update(@PathVariable Long id, @RequestBody ProjectRequest request, @CurrentUser UserPrincipal user) {
        return projectService.update(id, request, user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @CurrentUser UserPrincipal user) {
        projectService.delete(id, user);
    }

    @PostMapping("/{id}/scans")
    public ScanTaskDto scan(@PathVariable Long id, @CurrentUser UserPrincipal user) {
        return scanService.createManualScan(id, user);
    }

    @GetMapping("/{id}/scans")
    public List<ScanTaskDto> scans(@PathVariable Long id, @CurrentUser UserPrincipal user) {
        return scanService.listByProject(id, user).stream().map(ScanTaskDto::from).collect(Collectors.toList());
    }
}
