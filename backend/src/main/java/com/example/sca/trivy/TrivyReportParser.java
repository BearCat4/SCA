package com.example.sca.trivy;

import com.example.sca.common.Severity;
import com.example.sca.scan.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import org.springframework.stereotype.Component;

@Component
public class TrivyReportParser {
    private final ObjectMapper objectMapper;

    public TrivyReportParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public ParsedTrivyReport parse(String json) {
        try {
            ParsedTrivyReport report = new ParsedTrivyReport();
            JsonNode root = objectMapper.readTree(json == null ? "{}" : json);
            JsonNode results = root.path("Results");
            if (!results.isArray()) {
                return report;
            }
            for (JsonNode result : results) {
                String target = result.path("Target").asText("");
                String type = result.path("Type").asText("");
                parsePackages(report, result.path("Packages"), target, type);
                parseVulnerabilities(report, result.path("Vulnerabilities"), target);
            }
            return report;
        } catch (Exception ex) {
            throw new TrivyException("Failed to parse Trivy JSON", ex);
        }
    }

    private void parsePackages(ParsedTrivyReport report, JsonNode packages, String target, String type) {
        if (!packages.isArray()) {
            return;
        }
        for (JsonNode pkg : packages) {
            ComponentFinding component = new ComponentFinding();
            component.setTarget(target);
            component.setType(type);
            component.setPackageName(pkg.path("Name").asText(""));
            component.setVersion(pkg.path("Version").asText(""));
            report.getComponents().add(component);

            JsonNode licenses = pkg.path("Licenses");
            if (licenses.isArray()) {
                for (JsonNode licenseNode : licenses) {
                    LicenseFinding license = new LicenseFinding();
                    license.setTarget(target);
                    license.setPackageName(component.getPackageName());
                    license.setVersion(component.getVersion());
                    license.setLicenseName(licenseNode.asText(""));
                    report.getLicenses().add(license);
                }
            }
        }
    }

    private void parseVulnerabilities(ParsedTrivyReport report, JsonNode vulnerabilities, String target) {
        if (!vulnerabilities.isArray()) {
            return;
        }
        for (JsonNode node : vulnerabilities) {
            Vulnerability finding = new Vulnerability();
            finding.setTarget(target);
            finding.setVulnerabilityId(node.path("VulnerabilityID").asText(""));
            finding.setPackageName(node.path("PkgName").asText(""));
            finding.setInstalledVersion(node.path("InstalledVersion").asText(""));
            finding.setFixedVersion(node.path("FixedVersion").asText(""));
            finding.setSeverity(Severity.fromString(node.path("Severity").asText("UNKNOWN")));
            finding.setTitle(node.path("Title").asText(""));
            finding.setReferenceUrl(firstReference(node.path("References")));
            report.getVulnerabilities().add(finding);
        }
    }

    private String firstReference(JsonNode references) {
        if (!references.isArray()) {
            return "";
        }
        Iterator<JsonNode> iterator = references.elements();
        return iterator.hasNext() ? iterator.next().asText("") : "";
    }
}
