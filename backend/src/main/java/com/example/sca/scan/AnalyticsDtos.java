package com.example.sca.scan;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsDtos {
    public static class ProjectRiskSummary {
        public Long projectId;
        public String projectName;
        public String source;
        public String defaultBranch;
        public Long latestScanId;
        public ScanStatus latestStatus;
        public int vulnerabilityCount;
        public int criticalCount;
        public int highCount;
        public int mediumCount;
        public int lowCount;
        public int sbomCount;
        public int licenseCount;
    }

    public static class CveImpactSummary {
        public String cve;
        public long affectedProjectCount;
        public long findingCount;
        public List<CveImpactFinding> findings = new ArrayList<CveImpactFinding>();
    }

    public static class CveImpactFinding {
        public Long projectId;
        public String projectName;
        public Long scanId;
        public String target;
        public String packageName;
        public String installedVersion;
        public String fixedVersion;
        public String severity;
        public String title;
    }

    public static class LicenseTopItem {
        public String licenseName;
        public long count;
    }
}
