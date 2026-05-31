package com.example.sca.scan;

import com.example.sca.config.ScanProperties;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Component;

@Component
public class MavenDependencyTreeScanner implements DependencyTreeScanner {
    private final ScanProperties properties;
    private final MavenDependencyTreeParser parser;
    private final LockfileDependencyParser lockfileDependencyParser;

    public MavenDependencyTreeScanner(ScanProperties properties,
                                      MavenDependencyTreeParser parser,
                                      LockfileDependencyParser lockfileDependencyParser) {
        this.properties = properties;
        this.parser = parser;
        this.lockfileDependencyParser = lockfileDependencyParser;
    }

    @Override
    public List<DependencyEdge> scan(Path directory) {
        List<DependencyEdge> edges = new java.util.ArrayList<DependencyEdge>(lockfileDependencyParser.parse(directory));
        if (directory == null || !Files.exists(directory.resolve("pom.xml"))) {
            return edges;
        }
        edges.addAll(scanMaven(directory));
        return edges;
    }

    private List<DependencyEdge> scanMaven(Path directory) {
        if (directory == null || !Files.exists(directory.resolve("pom.xml"))) {
            return Collections.emptyList();
        }
        Path output = directory.resolve(".sca-dependencies.tgf");
        try {
            Process process = new ProcessBuilder(
                    "mvn",
                    "-q",
                    "dependency:tree",
                    "-DoutputType=tgf",
                    "-DoutputFile=" + output.toAbsolutePath().toString())
                    .directory(directory.toFile())
                    .redirectErrorStream(true)
                    .start();
            StreamCollector collector = new StreamCollector(process.getInputStream());
            Thread outputThread = new Thread(collector, "maven-dependency-tree");
            outputThread.start();
            boolean finished = process.waitFor(properties.getDependencyTimeoutSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new IllegalStateException("Maven dependency tree timed out after " + properties.getDependencyTimeoutSeconds() + " seconds");
            }
            outputThread.join();
            collector.throwIfFailed();
            if (process.exitValue() != 0) {
                throw new IllegalStateException("Maven dependency tree failed with exit code " + process.exitValue() + ": " + collector.asString());
            }
            if (!Files.exists(output)) {
                return Collections.emptyList();
            }
            return parser.parse(new String(Files.readAllBytes(output), StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to collect Maven dependency tree", ex);
        } finally {
            try {
                Files.deleteIfExists(output);
            } catch (Exception ignored) {
            }
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
                byte[] buffer = new byte[8192];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    output.write(buffer, 0, read);
                }
            } catch (Exception ex) {
                failure = ex;
            }
        }

        String asString() {
            return new String(output.toByteArray(), StandardCharsets.UTF_8);
        }

        void throwIfFailed() {
            if (failure != null) {
                throw new IllegalStateException("Failed to read Maven dependency tree output", failure);
            }
        }
    }
}
