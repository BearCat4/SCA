package com.example.sca.scan;

import com.example.sca.auth.CurrentUser;
import com.example.sca.auth.UserPrincipal;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/projects")
    public List<AnalyticsDtos.ProjectRiskSummary> projects(@CurrentUser UserPrincipal user) {
        return analyticsService.projectSummaries(user);
    }

    @GetMapping("/cves/{cve}")
    public AnalyticsDtos.CveImpactSummary cve(@PathVariable String cve, @CurrentUser UserPrincipal user) {
        return analyticsService.cveImpact(cve, user);
    }

    @GetMapping("/licenses/top")
    public List<AnalyticsDtos.LicenseTopItem> licenseTop(@CurrentUser UserPrincipal user) {
        return analyticsService.licenseTop(user);
    }
}
