package com.example.sca.scan;

import com.example.sca.common.Severity;

public class FindingDtos {
    public static class VulnerabilityDto {
        public Long id;
        public String target;
        public String vulnerabilityId;
        public String packageName;
        public String installedVersion;
        public String fixedVersion;
        public Severity severity;
        public VulnerabilityStatus status;
        public String title;
        public String referenceUrl;

        public static VulnerabilityDto from(Vulnerability finding) {
            VulnerabilityDto dto = new VulnerabilityDto();
            dto.id = finding.getId();
            dto.target = finding.getTarget();
            dto.vulnerabilityId = finding.getVulnerabilityId();
            dto.packageName = finding.getPackageName();
            dto.installedVersion = finding.getInstalledVersion();
            dto.fixedVersion = finding.getFixedVersion();
            dto.severity = finding.getSeverity();
            dto.status = finding.getStatus();
            dto.title = finding.getTitle();
            dto.referenceUrl = finding.getReferenceUrl();
            return dto;
        }
    }

    public static class ComponentDto {
        public Long id;
        public String target;
        public String packageName;
        public String version;
        public String type;

        public static ComponentDto from(ComponentFinding finding) {
            ComponentDto dto = new ComponentDto();
            dto.id = finding.getId();
            dto.target = finding.getTarget();
            dto.packageName = finding.getPackageName();
            dto.version = finding.getVersion();
            dto.type = finding.getType();
            return dto;
        }
    }

    public static class LicenseDto {
        public Long id;
        public String target;
        public String packageName;
        public String version;
        public String licenseName;

        public static LicenseDto from(LicenseFinding finding) {
            LicenseDto dto = new LicenseDto();
            dto.id = finding.getId();
            dto.target = finding.getTarget();
            dto.packageName = finding.getPackageName();
            dto.version = finding.getVersion();
            dto.licenseName = finding.getLicenseName();
            return dto;
        }
    }

    public static class DependencyDto {
        public Long id;
        public String sourceRef;
        public String sourceName;
        public String sourceVersion;
        public String targetRef;
        public String targetName;
        public String targetVersion;
        public String scope;

        public static DependencyDto from(DependencyEdge finding) {
            DependencyDto dto = new DependencyDto();
            dto.id = finding.getId();
            dto.sourceRef = finding.getSourceRef();
            dto.sourceName = finding.getSourceName();
            dto.sourceVersion = finding.getSourceVersion();
            dto.targetRef = finding.getTargetRef();
            dto.targetName = finding.getTargetName();
            dto.targetVersion = finding.getTargetVersion();
            dto.scope = finding.getScope();
            return dto;
        }
    }
}
