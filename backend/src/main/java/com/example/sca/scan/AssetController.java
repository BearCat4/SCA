package com.example.sca.scan;

import com.example.sca.auth.CurrentUser;
import com.example.sca.auth.UserPrincipal;
import com.example.sca.common.Severity;
import com.example.sca.project.ProjectDto;
import com.example.sca.project.ProjectService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assets")
public class AssetController {
    private final ProjectService projectService;
    private final ComponentRepository componentRepository;
    private final VulnerabilityRepository vulnerabilityRepository;

    public AssetController(ProjectService projectService,
                           ComponentRepository componentRepository,
                           VulnerabilityRepository vulnerabilityRepository) {
        this.projectService = projectService;
        this.componentRepository = componentRepository;
        this.vulnerabilityRepository = vulnerabilityRepository;
    }

    @GetMapping("/components")
    @Transactional(readOnly = true)
    public Page<AssetDtos.ComponentAssetDto> components(@CurrentUser UserPrincipal user,
                                                        @RequestParam(required = false) String q,
                                                        Pageable pageable) {
        List<Long> projectIds = projectIds(user);
        if (projectIds.isEmpty()) {
            return new PageImpl<AssetDtos.ComponentAssetDto>(java.util.Collections.emptyList(), pageable, 0);
        }
        return componentRepository.searchByProjectIds(projectIds, blankToNull(q), pageable).map(AssetDtos.ComponentAssetDto::from);
    }

    @GetMapping("/vulnerabilities")
    @Transactional(readOnly = true)
    public Page<AssetDtos.VulnerabilityAssetDto> vulnerabilities(@CurrentUser UserPrincipal user,
                                                                 @RequestParam(required = false) String q,
                                                                 @RequestParam(required = false) Severity severity,
                                                                 @RequestParam(required = false) VulnerabilityStatus status,
                                                                 Pageable pageable) {
        List<Long> projectIds = projectIds(user);
        if (projectIds.isEmpty()) {
            return new PageImpl<AssetDtos.VulnerabilityAssetDto>(java.util.Collections.emptyList(), pageable, 0);
        }
        return vulnerabilityRepository.searchByProjectIds(projectIds, blankToNull(q), severity, status, pageable).map(AssetDtos.VulnerabilityAssetDto::from);
    }

    private List<Long> projectIds(UserPrincipal user) {
        return projectService.list(user).stream().map(ProjectDto::getId).collect(Collectors.toList());
    }

    private String blankToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }
}
