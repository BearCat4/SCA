package com.example.sca.project;

import com.example.sca.auth.*;
import com.example.sca.common.ApiException;
import com.example.sca.scan.ComponentRepository;
import com.example.sca.scan.DependencyEdgeRepository;
import com.example.sca.scan.LicenseRepository;
import com.example.sca.scan.ScanTask;
import com.example.sca.scan.ScanTaskRepository;
import com.example.sca.scan.VulnerabilityRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final ScanTaskRepository scanTaskRepository;
    private final VulnerabilityRepository vulnerabilityRepository;
    private final ComponentRepository componentRepository;
    private final LicenseRepository licenseRepository;
    private final DependencyEdgeRepository dependencyEdgeRepository;

    public ProjectService(ProjectRepository projectRepository,
                          UserRepository userRepository,
                          TokenService tokenService,
                          ScanTaskRepository scanTaskRepository,
                          VulnerabilityRepository vulnerabilityRepository,
                          ComponentRepository componentRepository,
                          LicenseRepository licenseRepository,
                          DependencyEdgeRepository dependencyEdgeRepository) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.scanTaskRepository = scanTaskRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
        this.componentRepository = componentRepository;
        this.licenseRepository = licenseRepository;
        this.dependencyEdgeRepository = dependencyEdgeRepository;
    }

    @Transactional(readOnly = true)
    public List<ProjectDto> list(UserPrincipal user) {
        List<Project> projects = user.getRole() == UserRole.ADMIN
                ? projectRepository.findAll()
                : projectRepository.findByOwnerIdOrderByIdDesc(user.getId());
        return projects.stream().map(ProjectDto::from).collect(Collectors.toList());
    }

    @Transactional
    public ProjectDto create(ProjectRequest request, UserPrincipal user) {
        AppUser owner = userRepository.findById(user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
        String plainToken = tokenService.newProjectToken();
        Project project = new Project();
        apply(request, project);
        project.setOwner(owner);
        project.setTokenHash(tokenService.hashProjectToken(plainToken));
        Project saved = projectRepository.save(project);
        ProjectDto dto = ProjectDto.from(saved);
        dto.setProjectToken(plainToken);
        return dto;
    }

    @Transactional(readOnly = true)
    public Project getAccessible(Long id, UserPrincipal user) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Project not found"));
        if (user.getRole() != UserRole.ADMIN && !project.getOwner().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Project access denied");
        }
        return project;
    }

    @Transactional
    public ProjectDto update(Long id, ProjectRequest request, UserPrincipal user) {
        Project project = getAccessible(id, user);
        apply(request, project);
        return ProjectDto.from(projectRepository.save(project));
    }

    @Transactional
    public void delete(Long id, UserPrincipal user) {
        Project project = getAccessible(id, user);
        List<ScanTask> tasks = scanTaskRepository.findByProjectIdOrderByIdDesc(project.getId());
        for (ScanTask task : tasks) {
            vulnerabilityRepository.deleteByScanTaskId(task.getId());
            componentRepository.deleteByScanTaskId(task.getId());
            licenseRepository.deleteByScanTaskId(task.getId());
            dependencyEdgeRepository.deleteByScanTaskId(task.getId());
        }
        scanTaskRepository.deleteAll(tasks);
        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public Project findByToken(String token) {
        return projectRepository.findByTokenHash(tokenService.hashProjectToken(token))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid project token"));
    }

    private void apply(ProjectRequest request, Project project) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Project name is required");
        }
        if (request.getGitUrl() == null || request.getGitUrl().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Git URL or local path is required");
        }
        project.setName(request.getName().trim());
        project.setGitUrl(request.getGitUrl().trim());
        project.setDefaultBranch(request.getDefaultBranch() == null || request.getDefaultBranch().trim().isEmpty()
                ? "main" : request.getDefaultBranch().trim());
    }
}
