package com.example.sca.config;

import com.example.sca.auth.CurrentUser;
import com.example.sca.auth.UserPrincipal;
import com.example.sca.auth.UserRole;
import com.example.sca.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    private final ScanProperties scanProperties;

    public SettingsController(ScanProperties scanProperties) {
        this.scanProperties = scanProperties;
    }

    @GetMapping
    public SettingsDto get(@CurrentUser UserPrincipal user) {
        return dto(user);
    }

    @PutMapping
    public SettingsDto update(@RequestBody UpdateSettingsRequest request, @CurrentUser UserPrincipal user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role is required");
        }
        if (request.trivyBin != null && !request.trivyBin.trim().isEmpty()) {
            scanProperties.setTrivyBin(request.trivyBin.trim());
        }
        if (request.workDir != null && !request.workDir.trim().isEmpty()) {
            scanProperties.setWorkDir(request.workDir.trim());
        }
        if (request.timeoutSeconds != null && request.timeoutSeconds > 0) {
            scanProperties.setTimeoutSeconds(request.timeoutSeconds);
        }
        if (request.gitTimeoutSeconds != null && request.gitTimeoutSeconds > 0) {
            scanProperties.setGitTimeoutSeconds(request.gitTimeoutSeconds);
        }
        if (request.gitMaxAttempts != null && request.gitMaxAttempts > 0) {
            scanProperties.setGitMaxAttempts(request.gitMaxAttempts);
        }
        if (request.repositoryToken != null) {
            scanProperties.setRepositoryToken(request.repositoryToken.trim());
        }
        if (request.dependencyTimeoutSeconds != null && request.dependencyTimeoutSeconds > 0) {
            scanProperties.setDependencyTimeoutSeconds(request.dependencyTimeoutSeconds);
        }
        if (request.skipDbUpdate != null) {
            scanProperties.setSkipDbUpdate(request.skipDbUpdate);
        }
        return dto(user);
    }

    private SettingsDto dto(UserPrincipal user) {
        SettingsDto dto = new SettingsDto();
        dto.username = user.getUsername();
        dto.role = user.getRole().name();
        dto.trivyBin = scanProperties.getTrivyBin();
        dto.workDir = scanProperties.getWorkDir();
        dto.timeoutSeconds = scanProperties.getTimeoutSeconds();
        dto.gitTimeoutSeconds = scanProperties.getGitTimeoutSeconds();
        dto.gitMaxAttempts = scanProperties.getGitMaxAttempts();
        dto.hasRepositoryToken = scanProperties.getRepositoryToken() != null && !scanProperties.getRepositoryToken().trim().isEmpty();
        dto.dependencyTimeoutSeconds = scanProperties.getDependencyTimeoutSeconds();
        dto.skipDbUpdate = scanProperties.isSkipDbUpdate();
        return dto;
    }

    public static class UpdateSettingsRequest {
        public String trivyBin;
        public String workDir;
        public Long timeoutSeconds;
        public Long gitTimeoutSeconds;
        public Integer gitMaxAttempts;
        public String repositoryToken;
        public Long dependencyTimeoutSeconds;
        public Boolean skipDbUpdate;
    }

    public static class SettingsDto {
        public String username;
        public String role;
        public String trivyBin;
        public String workDir;
        public long timeoutSeconds;
        public long gitTimeoutSeconds;
        public int gitMaxAttempts;
        public boolean hasRepositoryToken;
        public long dependencyTimeoutSeconds;
        public boolean skipDbUpdate;
    }
}
