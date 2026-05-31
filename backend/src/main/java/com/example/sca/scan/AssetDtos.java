package com.example.sca.scan;

import com.example.sca.common.Severity;

public class AssetDtos {
    public static class ComponentAssetDto {
        public Long id;
        public Long projectId;
        public String projectName;
        public Long scanId;
        public String target;
        public String packageName;
        public String version;
        public String type;

        public static ComponentAssetDto from(ComponentFinding finding) {
            ComponentAssetDto dto = new ComponentAssetDto();
            dto.id = finding.getId();
            dto.projectId = finding.getScanTask().getProject().getId();
            dto.projectName = finding.getScanTask().getProject().getName();
            dto.scanId = finding.getScanTask().getId();
            dto.target = finding.getTarget();
            dto.packageName = finding.getPackageName();
            dto.version = finding.getVersion();
            dto.type = finding.getType();
            return dto;
        }
    }

    public static class VulnerabilityAssetDto {
        public Long id;
        public Long projectId;
        public String projectName;
        public Long scanId;
        public String target;
        public String vulnerabilityId;
        public String packageName;
        public String installedVersion;
        public String fixedVersion;
        public Severity severity;
        public VulnerabilityStatus status;
        public String title;

        public static VulnerabilityAssetDto from(Vulnerability finding) {
            VulnerabilityAssetDto dto = new VulnerabilityAssetDto();
            dto.id = finding.getId();
            dto.projectId = finding.getScanTask().getProject().getId();
            dto.projectName = finding.getScanTask().getProject().getName();
            dto.scanId = finding.getScanTask().getId();
            dto.target = finding.getTarget();
            dto.vulnerabilityId = finding.getVulnerabilityId();
            dto.packageName = finding.getPackageName();
            dto.installedVersion = finding.getInstalledVersion();
            dto.fixedVersion = finding.getFixedVersion();
            dto.severity = finding.getSeverity();
            dto.status = finding.getStatus();
            dto.title = finding.getTitle();
            return dto;
        }
    }
}
