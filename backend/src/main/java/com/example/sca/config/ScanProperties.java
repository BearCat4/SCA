package com.example.sca.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sca.scan")
public class ScanProperties {
    private String trivyBin = "trivy";
    private String workDir = System.getProperty("java.io.tmpdir") + "/sca-work";
    private long timeoutSeconds = 600;
    private long gitTimeoutSeconds = 180;
    private int gitMaxAttempts = 3;
    private String repositoryToken = "";
    private boolean failOnCritical = true;
    private boolean failOnHigh = true;
    private String forbiddenLicenses = "GPL,AGPL,LGPL";
    private long dependencyTimeoutSeconds = 180;
    private boolean skipDbUpdate = true;

    public String getTrivyBin() {
        return trivyBin;
    }

    public void setTrivyBin(String trivyBin) {
        this.trivyBin = trivyBin;
    }

    public String getWorkDir() {
        return workDir;
    }

    public void setWorkDir(String workDir) {
        this.workDir = workDir;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public long getGitTimeoutSeconds() {
        return gitTimeoutSeconds;
    }

    public void setGitTimeoutSeconds(long gitTimeoutSeconds) {
        this.gitTimeoutSeconds = gitTimeoutSeconds;
    }

    public int getGitMaxAttempts() {
        return gitMaxAttempts;
    }

    public void setGitMaxAttempts(int gitMaxAttempts) {
        this.gitMaxAttempts = gitMaxAttempts;
    }

    public String getRepositoryToken() {
        return repositoryToken;
    }

    public void setRepositoryToken(String repositoryToken) {
        this.repositoryToken = repositoryToken;
    }

    public boolean isFailOnCritical() {
        return failOnCritical;
    }

    public void setFailOnCritical(boolean failOnCritical) {
        this.failOnCritical = failOnCritical;
    }

    public boolean isFailOnHigh() {
        return failOnHigh;
    }

    public void setFailOnHigh(boolean failOnHigh) {
        this.failOnHigh = failOnHigh;
    }

    public String getForbiddenLicenses() {
        return forbiddenLicenses;
    }

    public void setForbiddenLicenses(String forbiddenLicenses) {
        this.forbiddenLicenses = forbiddenLicenses;
    }

    public long getDependencyTimeoutSeconds() {
        return dependencyTimeoutSeconds;
    }

    public void setDependencyTimeoutSeconds(long dependencyTimeoutSeconds) {
        this.dependencyTimeoutSeconds = dependencyTimeoutSeconds;
    }

    public boolean isSkipDbUpdate() {
        return skipDbUpdate;
    }

    public void setSkipDbUpdate(boolean skipDbUpdate) {
        this.skipDbUpdate = skipDbUpdate;
    }
}
