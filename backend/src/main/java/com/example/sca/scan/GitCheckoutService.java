package com.example.sca.scan;

import com.example.sca.config.ScanProperties;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GitCheckoutService {
    private final ScanProperties properties;
    private final GitCommandRunner commandRunner;

    @Autowired
    public GitCheckoutService(ScanProperties properties) {
        this(properties, new ProcessGitCommandRunner());
    }

    GitCheckoutService(ScanProperties properties, GitCommandRunner commandRunner) {
        this.properties = properties;
        this.commandRunner = commandRunner;
    }

    public void checkout(String gitUrl, String branch, Path destination) {
        List<String> command = Arrays.asList("git", "clone", "--depth", "1", "--branch", branch, withRepositoryToken(gitUrl), destination.toString());
        RuntimeException lastError = null;
        int attempts = Math.max(1, properties.getGitMaxAttempts());
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                GitCommandResult result = commandRunner.run(command, properties.getGitTimeoutSeconds());
                if (!result.isFinished()) {
                    throw new IllegalStateException("Git clone timed out after " + properties.getGitTimeoutSeconds() + " seconds");
                }
                if (result.getExitCode() != 0) {
                    throw new IllegalStateException("Git clone failed with exit code " + result.getExitCode() + ": " + result.getOutput());
                }
                return;
            } catch (RuntimeException ex) {
                lastError = ex;
            }
            if (attempt < attempts) {
                sleepBeforeRetry(attempt);
            }
        }
        throw new IllegalStateException("Git clone failed after " + attempts + " attempt(s): " + lastError.getMessage(), lastError);
    }

    private String withRepositoryToken(String gitUrl) {
        String token = properties.getRepositoryToken();
        if (gitUrl == null || token == null || token.trim().isEmpty() || !gitUrl.startsWith("https://")) {
            return gitUrl;
        }
        if (gitUrl.substring("https://".length()).contains("@")) {
            return gitUrl;
        }
        return "https://" + token.trim() + "@" + gitUrl.substring("https://".length());
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(Math.min(5000L, 1000L * attempt));
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while retrying Git clone", ex);
        }
    }

    interface GitCommandRunner {
        GitCommandResult run(List<String> command, long timeoutSeconds);
    }

    static class GitCommandResult {
        private final boolean finished;
        private final int exitCode;
        private final String output;

        GitCommandResult(boolean finished, int exitCode, String output) {
            this.finished = finished;
            this.exitCode = exitCode;
            this.output = output;
        }

        boolean isFinished() {
            return finished;
        }

        int getExitCode() {
            return exitCode;
        }

        String getOutput() {
            return output;
        }
    }

    static class ProcessGitCommandRunner implements GitCommandRunner {
        @Override
        public GitCommandResult run(List<String> command, long timeoutSeconds) {
            try {
                Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
                boolean finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                if (!finished) {
                    process.destroyForcibly();
                    copy(process.getInputStream(), output);
                    return new GitCommandResult(false, -1, new String(output.toByteArray(), StandardCharsets.UTF_8));
                }
                copy(process.getInputStream(), output);
                return new GitCommandResult(true, process.exitValue(), new String(output.toByteArray(), StandardCharsets.UTF_8));
            } catch (Exception ex) {
                throw new IllegalStateException("Git command failed", ex);
            }
        }

        private void copy(InputStream input, ByteArrayOutputStream output) throws Exception {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                output.write(buffer, 0, read);
            }
        }
    }
}
