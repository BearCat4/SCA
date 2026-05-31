package com.example.sca.scan;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class MavenDependencyTreeParserTest {
    private final MavenDependencyTreeParser parser = new MavenDependencyTreeParser();

    @Test
    void parsesTgfDependencyEdges() {
        String tgf = ""
                + "1 com.example:demo:jar:1.0.0\n"
                + "2 org.springframework:spring-core:jar:5.3.31:compile\n"
                + "3 commons-logging:commons-logging:jar:1.2:compile\n"
                + "#\n"
                + "1 2 compile\n"
                + "2 3 runtime\n";

        List<DependencyEdge> edges = parser.parse(tgf);

        assertThat(edges).hasSize(2);
        assertThat(edges.get(0).getSourceName()).isEqualTo("com.example:demo");
        assertThat(edges.get(0).getSourceVersion()).isEqualTo("1.0.0");
        assertThat(edges.get(0).getTargetName()).isEqualTo("org.springframework:spring-core");
        assertThat(edges.get(0).getTargetVersion()).isEqualTo("5.3.31");
        assertThat(edges.get(0).getScope()).isEqualTo("compile");
    }
}
