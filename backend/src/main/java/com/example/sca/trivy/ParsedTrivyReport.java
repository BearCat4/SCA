package com.example.sca.trivy;

import com.example.sca.scan.ComponentFinding;
import com.example.sca.scan.LicenseFinding;
import com.example.sca.scan.Vulnerability;
import java.util.ArrayList;
import java.util.List;

public class ParsedTrivyReport {
    private final List<Vulnerability> vulnerabilities = new ArrayList<Vulnerability>();
    private final List<ComponentFinding> components = new ArrayList<ComponentFinding>();
    private final List<LicenseFinding> licenses = new ArrayList<LicenseFinding>();

    public List<Vulnerability> getVulnerabilities() {
        return vulnerabilities;
    }

    public List<ComponentFinding> getComponents() {
        return components;
    }

    public List<LicenseFinding> getLicenses() {
        return licenses;
    }
}
