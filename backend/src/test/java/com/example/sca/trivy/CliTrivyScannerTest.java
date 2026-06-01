package com.example.sca.trivy;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sca.config.ScanProperties;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CliTrivyScannerTest {
    @TempDir
    Path tempDir;

    @Test
    void includesAllPackagesInFilesystemScans() throws Exception {
        Path argsFile = tempDir.resolve("args.txt");
        CliTrivyScanner scanner = scannerWithFakeTrivy(argsFile);

        scanner.scan(tempDir);

        assertThat(Files.readAllLines(argsFile)).contains("--list-all-pkgs");
    }

    @Test
    void includesAllPackagesInImageScans() throws Exception {
        Path argsFile = tempDir.resolve("args.txt");
        CliTrivyScanner scanner = scannerWithFakeTrivy(argsFile);

        scanner.scanImage("nginx:latest");

        assertThat(Files.readAllLines(argsFile)).contains("--list-all-pkgs");
    }

    private CliTrivyScanner scannerWithFakeTrivy(Path argsFile) throws Exception {
        Path fakeTrivy = tempDir.resolve("fake-trivy.sh");
        String script = "#!/bin/sh\n"
                + "printf '%s\\n' \"$@\" > \"" + argsFile.toAbsolutePath() + "\"\n"
                + "printf '{\"Results\":[]}'\n";
        Files.write(fakeTrivy, script.getBytes(StandardCharsets.UTF_8));
        fakeTrivy.toFile().setExecutable(true);

        ScanProperties properties = new ScanProperties();
        properties.setTrivyBin(fakeTrivy.toAbsolutePath().toString());
        properties.setTimeoutSeconds(5);
        properties.setSkipDbUpdate(false);
        return new CliTrivyScanner(properties);
    }
}
