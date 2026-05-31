package com.example.sca.trivy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.sca.common.Severity;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class TrivyReportParserTest {
    private final TrivyReportParser parser = new TrivyReportParser(new ObjectMapper());

    @Test
    void parsesVulnerabilitiesComponentsAndLicenses() {
        String json = "{ \"Results\": [{ \"Target\": \"pom.xml\", \"Type\": \"maven\","
                + "\"Packages\": [{\"Name\":\"spring-core\",\"Version\":\"5.3.31\",\"Licenses\":[\"Apache-2.0\"]}],"
                + "\"Vulnerabilities\": [{\"VulnerabilityID\":\"CVE-1\",\"PkgName\":\"spring-core\","
                + "\"InstalledVersion\":\"5.3.31\",\"FixedVersion\":\"5.3.32\",\"Severity\":\"HIGH\","
                + "\"Title\":\"Example vuln\",\"References\":[\"https://example.test/cve\"]}] }]}";

        ParsedTrivyReport report = parser.parse(json);

        assertThat(report.getVulnerabilities()).hasSize(1);
        assertThat(report.getVulnerabilities().get(0).getSeverity()).isEqualTo(Severity.HIGH);
        assertThat(report.getVulnerabilities().get(0).getReferenceUrl()).isEqualTo("https://example.test/cve");
        assertThat(report.getComponents()).hasSize(1);
        assertThat(report.getLicenses()).hasSize(1);
        assertThat(report.getLicenses().get(0).getLicenseName()).isEqualTo("Apache-2.0");
    }

    @Test
    void returnsEmptyReportWhenResultsMissing() {
        ParsedTrivyReport report = parser.parse("{}");

        assertThat(report.getVulnerabilities()).isEmpty();
        assertThat(report.getComponents()).isEmpty();
        assertThat(report.getLicenses()).isEmpty();
    }

    @Test
    void rejectsInvalidJson() {
        assertThatThrownBy(() -> parser.parse("{"))
                .isInstanceOf(TrivyException.class)
                .hasMessageContaining("Failed to parse Trivy JSON");
    }

    @Test
    void supportsContainerImageTargets() {
        String json = "{ \"Results\": [{ \"Target\": \"nginx:latest\", \"Type\": \"debian\","
                + "\"Packages\": [{\"Name\":\"openssl\",\"Version\":\"3.0.0\",\"Licenses\":[\"Apache-2.0\"]}],"
                + "\"Vulnerabilities\": [{\"VulnerabilityID\":\"CVE-IMG-1\",\"PkgName\":\"openssl\","
                + "\"InstalledVersion\":\"3.0.0\",\"FixedVersion\":\"3.0.1\",\"Severity\":\"CRITICAL\","
                + "\"Title\":\"Image vuln\"}] }]}";

        ParsedTrivyReport report = parser.parse(json);

        assertThat(report.getVulnerabilities()).hasSize(1);
        assertThat(report.getVulnerabilities().get(0).getTarget()).isEqualTo("nginx:latest");
        assertThat(report.getComponents().get(0).getTarget()).isEqualTo("nginx:latest");
    }
}
