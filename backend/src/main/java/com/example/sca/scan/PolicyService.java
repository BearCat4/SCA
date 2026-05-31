package com.example.sca.scan;

import com.example.sca.common.Severity;
import com.example.sca.config.ScanProperties;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PolicyService {
    private final ScanProperties scanProperties;

    public PolicyService(ScanProperties scanProperties) {
        this.scanProperties = scanProperties;
    }

    public ScanStatus decideStatus(List<Vulnerability> vulnerabilities) {
        for (Vulnerability vulnerability : vulnerabilities) {
            if (violatesVulnerabilityPolicy(vulnerability)) {
                return ScanStatus.FAILED;
            }
        }
        return ScanStatus.PASSED;
    }

    private boolean violatesVulnerabilityPolicy(Vulnerability vulnerability) {
        if (vulnerability.getSeverity() == Severity.CRITICAL && scanProperties.isFailOnCritical()) {
            return true;
        }
        return vulnerability.getSeverity() == Severity.HIGH && scanProperties.isFailOnHigh();
    }
}
