package com.example.sca.scan;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LockfileDependencyParserTest {
    @TempDir
    Path tempDir;

    private final LockfileDependencyParser parser = new LockfileDependencyParser(new ObjectMapper());

    @Test
    void parsesNpmPackageLockDependencies() throws Exception {
        Files.write(tempDir.resolve("package-lock.json"), ("{"
                + "\"name\":\"web-app\",\"version\":\"1.0.0\",\"lockfileVersion\":3,"
                + "\"packages\":{"
                + "\"\":{\"name\":\"web-app\",\"version\":\"1.0.0\",\"dependencies\":{\"vue\":\"3.4.0\"}},"
                + "\"node_modules/vue\":{\"version\":\"3.4.0\",\"dependencies\":{\"@vue/shared\":\"3.4.0\"}},"
                + "\"node_modules/@vue/shared\":{\"version\":\"3.4.0\"}"
                + "}}").getBytes(StandardCharsets.UTF_8));

        List<DependencyEdge> edges = parser.parse(tempDir);

        assertThat(edges).hasSize(2);
        assertThat(edges.get(0).getSourceName()).isEqualTo("web-app");
        assertThat(edges.get(0).getTargetName()).isEqualTo("vue");
        assertThat(edges.get(0).getTargetVersion()).isEqualTo("3.4.0");
        assertThat(edges.get(0).getScope()).isEqualTo("npm");
        assertThat(edges.get(1).getSourceName()).isEqualTo("vue");
        assertThat(edges.get(1).getTargetName()).isEqualTo("@vue/shared");
    }

    @Test
    void parsesPythonRequirements() throws Exception {
        Files.write(tempDir.resolve("requirements.txt"), ("flask==3.0.0\n"
                + "requests>=2.31.0\n"
                + "# ignored\n").getBytes(StandardCharsets.UTF_8));

        List<DependencyEdge> edges = parser.parse(tempDir);

        assertThat(edges).hasSize(2);
        assertThat(edges.get(0).getSourceName()).isEqualTo("requirements.txt");
        assertThat(edges.get(0).getTargetName()).isEqualTo("flask");
        assertThat(edges.get(0).getTargetVersion()).isEqualTo("3.0.0");
        assertThat(edges.get(1).getTargetName()).isEqualTo("requests");
        assertThat(edges.get(1).getTargetVersion()).isEqualTo(">=2.31.0");
    }

    @Test
    void parsesGradleLockfile() throws Exception {
        Files.write(tempDir.resolve("gradle.lockfile"), ("com.fasterxml.jackson.core:jackson-databind:2.13.5=compileClasspath,runtimeClasspath\n"
                + "empty=ignored\n").getBytes(StandardCharsets.UTF_8));

        List<DependencyEdge> edges = parser.parse(tempDir);

        assertThat(edges).hasSize(1);
        assertThat(edges.get(0).getSourceName()).isEqualTo("gradle.lockfile");
        assertThat(edges.get(0).getTargetName()).isEqualTo("com.fasterxml.jackson.core:jackson-databind");
        assertThat(edges.get(0).getTargetVersion()).isEqualTo("2.13.5");
        assertThat(edges.get(0).getScope()).isEqualTo("compileClasspath,runtimeClasspath");
    }

    @Test
    void parsesPoetryLockDependencies() throws Exception {
        Files.write(tempDir.resolve("poetry.lock"), ("[[package]]\n"
                + "name = \"fastapi\"\n"
                + "version = \"0.110.0\"\n"
                + "[package.dependencies]\n"
                + "starlette = \">=0.36.3,<0.37.0\"\n"
                + "\n"
                + "[[package]]\n"
                + "name = \"starlette\"\n"
                + "version = \"0.36.3\"\n").getBytes(StandardCharsets.UTF_8));

        List<DependencyEdge> edges = parser.parse(tempDir);

        assertThat(edges).hasSize(1);
        assertThat(edges.get(0).getSourceName()).isEqualTo("fastapi");
        assertThat(edges.get(0).getTargetName()).isEqualTo("starlette");
        assertThat(edges.get(0).getTargetVersion()).isEqualTo("0.36.3");
        assertThat(edges.get(0).getScope()).isEqualTo("poetry");
    }

    @Test
    void parsesGoModDependencies() throws Exception {
        Files.write(tempDir.resolve("go.mod"), ("module github.com/example/service\n"
                + "\n"
                + "go 1.22\n"
                + "\n"
                + "require (\n"
                + "\tgithub.com/gin-gonic/gin v1.10.0\n"
                + "\tgolang.org/x/crypto v0.24.0 // indirect\n"
                + ")\n").getBytes(StandardCharsets.UTF_8));

        List<DependencyEdge> edges = parser.parse(tempDir);

        assertThat(edges).hasSize(2);
        assertThat(edges.get(0).getSourceName()).isEqualTo("github.com/example/service");
        assertThat(edges.get(0).getTargetName()).isEqualTo("github.com/gin-gonic/gin");
        assertThat(edges.get(0).getTargetVersion()).isEqualTo("v1.10.0");
        assertThat(edges.get(0).getScope()).isEqualTo("go");
        assertThat(edges.get(1).getTargetName()).isEqualTo("golang.org/x/crypto");
        assertThat(edges.get(1).getScope()).isEqualTo("go:indirect");
    }
}
