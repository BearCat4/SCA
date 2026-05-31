package com.example.sca.trivy;

import com.example.sca.config.ScanProperties;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class CliTrivyScanner implements TrivyScanner {
    private final ScanProperties properties;

    public CliTrivyScanner(ScanProperties properties) {
        this.properties = properties;
    }

    @Override
    public String scan(Path directory) {
        List<String> command = baseCommand("fs");
        command.add(directory.toAbsolutePath().toString());
        return run(command);
    }

    @Override
    public String scanImage(String imageRef) {
        List<String> command = baseCommand("image");
        command.add(imageRef);
        return run(command);
    }

    private List<String> baseCommand(String targetType) {
        List<String> command = new ArrayList<String>();
        command.add(properties.getTrivyBin());
        command.add(targetType);
        command.add("--format");
        command.add("json");
        command.add("--scanners");
        command.add("vuln,license");
        command.add("--timeout");
        command.add(properties.getTimeoutSeconds() + "s");
        command.add("--no-progress");
        if (properties.isSkipDbUpdate()) {
            command.add("--skip-db-update");
            command.add("--skip-java-db-update");
        }
        return command;
    }

    private String run(List<String> command) {
        try {
            Process process = new ProcessBuilder(command).redirectErrorStream(false).start();
            StreamCollector stdout = new StreamCollector(process.getInputStream());
            StreamCollector stderr = new StreamCollector(process.getErrorStream());
            Thread stdoutThread = new Thread(stdout, "trivy-stdout");
            Thread stderrThread = new Thread(stderr, "trivy-stderr");
            stdoutThread.start();
            stderrThread.start();
            boolean finished = process.waitFor(properties.getTimeoutSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new TrivyException("Trivy scan timed out after " + properties.getTimeoutSeconds() + " seconds");
            }
            stdoutThread.join();
            stderrThread.join();
            stdout.throwIfFailed();
            stderr.throwIfFailed();
            String output = stdout.asString();
            String error = stderr.asString();
            if (process.exitValue() != 0) {
                throw new TrivyException("Trivy exited with code " + process.exitValue() + ": " + error + output);
            }
            return output;
        } catch (TrivyException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new TrivyException("Failed to run Trivy", ex);
        }
    }

    private void copy(InputStream input, ByteArrayOutputStream output) throws Exception {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) >= 0) {
            output.write(buffer, 0, read);
        }
    }

    private class StreamCollector implements Runnable {
        private final InputStream input;
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();
        private Exception failure;

        StreamCollector(InputStream input) {
            this.input = input;
        }

        @Override
        public void run() {
            try {
                copy(input, output);
            } catch (Exception ex) {
                failure = ex;
            }
        }

        String asString() {
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }

        void throwIfFailed() {
            if (failure != null) {
                throw new TrivyException("Failed to read Trivy output", failure);
            }
        }
    }
}
