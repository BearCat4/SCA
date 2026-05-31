package com.example.sca.scan;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SourcePathResolverTest {
    private final SourcePathResolver resolver = new SourcePathResolver();

    @TempDir
    Path tempDir;

    @Test
    void resolvesExistingLocalDirectory() {
        assertThat(resolver.localDirectoryOrNull(tempDir.toString())).isEqualTo(tempDir.toAbsolutePath().normalize());
    }

    @Test
    void resolvesFileUriDirectory() {
        assertThat(resolver.localDirectoryOrNull(tempDir.toUri().toString())).isEqualTo(tempDir.toAbsolutePath().normalize());
    }

    @Test
    void ignoresUrlsAndMissingPaths() throws Exception {
        Path file = Files.createFile(tempDir.resolve("pom.xml"));

        assertThat(resolver.localDirectoryOrNull("https://github.com/WebGoat/WebGoat/")).isNull();
        assertThat(resolver.localDirectoryOrNull(tempDir.resolve("missing").toString())).isNull();
    }

    @Test
    void resolvesExistingLocalFilesForUploadScans() throws Exception {
        Path file = Files.createFile(tempDir.resolve("nacos-develop.zip"));

        assertThat(resolver.localDirectoryOrNull(file.toString())).isEqualTo(file.toAbsolutePath().normalize());
    }
}
