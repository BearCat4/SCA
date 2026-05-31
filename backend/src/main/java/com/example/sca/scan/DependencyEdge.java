package com.example.sca.scan;

import javax.persistence.*;

@Entity
@Table(name = "dependency_edges")
public class DependencyEdge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ScanTask scanTask;

    @Column(length = 1000)
    private String sourceRef;

    @Column(length = 500)
    private String sourceName;

    private String sourceVersion;

    @Column(length = 1000)
    private String targetRef;

    @Column(length = 500)
    private String targetName;

    private String targetVersion;

    private String scope;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ScanTask getScanTask() { return scanTask; }
    public void setScanTask(ScanTask scanTask) { this.scanTask = scanTask; }
    public String getSourceRef() { return sourceRef; }
    public void setSourceRef(String sourceRef) { this.sourceRef = sourceRef; }
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }
    public String getSourceVersion() { return sourceVersion; }
    public void setSourceVersion(String sourceVersion) { this.sourceVersion = sourceVersion; }
    public String getTargetRef() { return targetRef; }
    public void setTargetRef(String targetRef) { this.targetRef = targetRef; }
    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }
    public String getTargetVersion() { return targetVersion; }
    public void setTargetVersion(String targetVersion) { this.targetVersion = targetVersion; }
    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
}
