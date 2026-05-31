package com.example.sca.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sca.auth.UserPrincipal;
import com.example.sca.auth.UserRole;
import org.junit.jupiter.api.Test;

class SettingsControllerTest {
    @Test
    void updatesMutableScanSettings() {
        ScanProperties properties = new ScanProperties();
        SettingsController controller = new SettingsController(properties);
        SettingsController.UpdateSettingsRequest request = new SettingsController.UpdateSettingsRequest();
        request.trivyBin = "/opt/homebrew/bin/trivy";
        request.workDir = "/tmp/sca";
        request.timeoutSeconds = 900L;
        request.gitTimeoutSeconds = 240L;
        request.gitMaxAttempts = 4;
        request.dependencyTimeoutSeconds = 300L;
        request.skipDbUpdate = false;

        SettingsController.SettingsDto result = controller.update(request, user());

        assertThat(result.trivyBin).isEqualTo("/opt/homebrew/bin/trivy");
        assertThat(result.workDir).isEqualTo("/tmp/sca");
        assertThat(result.timeoutSeconds).isEqualTo(900L);
        assertThat(result.gitTimeoutSeconds).isEqualTo(240L);
        assertThat(result.gitMaxAttempts).isEqualTo(4);
        assertThat(result.dependencyTimeoutSeconds).isEqualTo(300L);
        assertThat(result.skipDbUpdate).isFalse();
    }

    private UserPrincipal user() {
        return new UserPrincipal(1L, "admin", UserRole.ADMIN);
    }
}
