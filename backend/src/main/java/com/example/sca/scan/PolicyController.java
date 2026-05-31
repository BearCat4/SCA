package com.example.sca.scan;

import com.example.sca.auth.CurrentUser;
import com.example.sca.auth.UserPrincipal;
import com.example.sca.auth.UserRole;
import com.example.sca.common.ApiException;
import com.example.sca.config.ScanProperties;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/policies")
public class PolicyController {
    private final ScanProperties scanProperties;

    public PolicyController(ScanProperties scanProperties) {
        this.scanProperties = scanProperties;
    }

    @GetMapping
    public PolicyDto get() {
        return dto();
    }

    @PutMapping
    public PolicyDto update(@RequestBody PolicyDto request, @CurrentUser UserPrincipal user) {
        if (user.getRole() != UserRole.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role is required");
        }
        scanProperties.setFailOnCritical(request.failOnCritical);
        scanProperties.setFailOnHigh(request.failOnHigh);
        scanProperties.setForbiddenLicenses(request.forbiddenLicenses == null ? "" : request.forbiddenLicenses.trim());
        return dto();
    }

    private PolicyDto dto() {
        PolicyDto dto = new PolicyDto();
        dto.failOnCritical = scanProperties.isFailOnCritical();
        dto.failOnHigh = scanProperties.isFailOnHigh();
        dto.forbiddenLicenses = scanProperties.getForbiddenLicenses();
        return dto;
    }

    public static class PolicyDto {
        public boolean failOnCritical;
        public boolean failOnHigh;
        public String forbiddenLicenses;
    }
}
