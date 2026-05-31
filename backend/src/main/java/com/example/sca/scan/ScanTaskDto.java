package com.example.sca.scan;

import java.time.Instant;

public class ScanTaskDto {
    private Long id;
    private Long projectId;
    private String projectName;
    private String branch;
    private ScanTrigger triggerType;
    private ScanStatus status;
    private Instant startedAt;
    private Instant finishedAt;
    private int vulnerabilityCount;
    private int criticalCount;
    private int highCount;
    private int mediumCount;
    private int lowCount;
    private int componentCount;
    private int licenseCount;
    private String failureReason;

    public static ScanTaskDto from(ScanTask task) {
        ScanTaskDto dto = new ScanTaskDto();
        dto.setId(task.getId());
        dto.setProjectId(task.getProject().getId());
        dto.setProjectName(task.getProject().getName());
        dto.setBranch(task.getBranch());
        dto.setTriggerType(task.getTriggerType());
        dto.setStatus(task.getStatus());
        dto.setStartedAt(task.getStartedAt());
        dto.setFinishedAt(task.getFinishedAt());
        dto.setVulnerabilityCount(task.getVulnerabilityCount());
        dto.setCriticalCount(task.getCriticalCount());
        dto.setHighCount(task.getHighCount());
        dto.setMediumCount(task.getMediumCount());
        dto.setLowCount(task.getLowCount());
        dto.setComponentCount(task.getComponentCount());
        dto.setLicenseCount(task.getLicenseCount());
        dto.setFailureReason(task.getFailureReason());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }
    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }
    public String getBranch() { return branch; }
    public void setBranch(String branch) { this.branch = branch; }
    public ScanTrigger getTriggerType() { return triggerType; }
    public void setTriggerType(ScanTrigger triggerType) { this.triggerType = triggerType; }
    public ScanStatus getStatus() { return status; }
    public void setStatus(ScanStatus status) { this.status = status; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }
    public int getVulnerabilityCount() { return vulnerabilityCount; }
    public void setVulnerabilityCount(int vulnerabilityCount) { this.vulnerabilityCount = vulnerabilityCount; }
    public int getCriticalCount() { return criticalCount; }
    public void setCriticalCount(int criticalCount) { this.criticalCount = criticalCount; }
    public int getHighCount() { return highCount; }
    public void setHighCount(int highCount) { this.highCount = highCount; }
    public int getMediumCount() { return mediumCount; }
    public void setMediumCount(int mediumCount) { this.mediumCount = mediumCount; }
    public int getLowCount() { return lowCount; }
    public void setLowCount(int lowCount) { this.lowCount = lowCount; }
    public int getComponentCount() { return componentCount; }
    public void setComponentCount(int componentCount) { this.componentCount = componentCount; }
    public int getLicenseCount() { return licenseCount; }
    public void setLicenseCount(int licenseCount) { this.licenseCount = licenseCount; }
    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
}
