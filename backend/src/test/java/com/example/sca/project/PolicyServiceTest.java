package com.example.sca.project;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sca.common.Severity;
import com.example.sca.config.ScanProperties;
import com.example.sca.scan.*;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class PolicyServiceTest {
    private final ScanProperties scanProperties = new ScanProperties();
    private final PolicyService service = new PolicyService(scanProperties);

    @Test
    void failsWhenSeverityViolatesPolicy() {
        Vulnerability high = vulnerability(Severity.HIGH);
        Vulnerability medium = vulnerability(Severity.MEDIUM);

        ScanStatus status = service.decideStatus(Arrays.asList(medium, high));

        assertThat(status).isEqualTo(ScanStatus.FAILED);
    }

    @Test
    void passesWhenFindingsDoNotViolatePolicy() {
        ScanStatus status = service.decideStatus(Collections.singletonList(vulnerability(Severity.MEDIUM)));

        assertThat(status).isEqualTo(ScanStatus.PASSED);
    }

    @Test
    void passesHighFindingsWhenHighPolicyIsDisabled() {
        scanProperties.setFailOnHigh(false);

        ScanStatus status = service.decideStatus(Collections.singletonList(vulnerability(Severity.HIGH)));

        assertThat(status).isEqualTo(ScanStatus.PASSED);
    }

    @Test
    void passesWhenThereAreNoVulnerabilities() {
        ScanStatus status = service.decideStatus(Collections.<Vulnerability>emptyList());

        assertThat(status).isEqualTo(ScanStatus.PASSED);
    }

    private Vulnerability vulnerability(Severity severity) {
        Vulnerability vulnerability = new Vulnerability();
        vulnerability.setSeverity(severity);
        return vulnerability;
    }
}
