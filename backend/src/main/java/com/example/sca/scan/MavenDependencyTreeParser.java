package com.example.sca.scan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MavenDependencyTreeParser {
    public List<DependencyEdge> parse(String tgf) {
        Map<String, ArtifactRef> nodes = new HashMap<String, ArtifactRef>();
        List<DependencyEdge> edges = new ArrayList<DependencyEdge>();
        boolean readingEdges = false;
        String[] lines = tgf == null ? new String[0] : tgf.split("\\r?\\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            if ("#".equals(trimmed)) {
                readingEdges = true;
                continue;
            }
            if (readingEdges) {
                parseEdge(trimmed, nodes, edges);
            } else {
                parseNode(trimmed, nodes);
            }
        }
        return edges;
    }

    private void parseNode(String line, Map<String, ArtifactRef> nodes) {
        int split = line.indexOf(' ');
        if (split <= 0 || split == line.length() - 1) {
            return;
        }
        nodes.put(line.substring(0, split), ArtifactRef.from(line.substring(split + 1)));
    }

    private void parseEdge(String line, Map<String, ArtifactRef> nodes, List<DependencyEdge> edges) {
        String[] parts = line.split("\\s+");
        if (parts.length < 2) {
            return;
        }
        ArtifactRef source = nodes.get(parts[0]);
        ArtifactRef target = nodes.get(parts[1]);
        if (source == null || target == null) {
            return;
        }
        DependencyEdge edge = new DependencyEdge();
        edge.setSourceRef(source.ref);
        edge.setSourceName(source.name);
        edge.setSourceVersion(source.version);
        edge.setTargetRef(target.ref);
        edge.setTargetName(target.name);
        edge.setTargetVersion(target.version);
        edge.setScope(parts.length > 2 ? parts[2] : "");
        edges.add(edge);
    }

    private static class ArtifactRef {
        private final String ref;
        private final String name;
        private final String version;

        private ArtifactRef(String ref, String name, String version) {
            this.ref = ref;
            this.name = name;
            this.version = version;
        }

        static ArtifactRef from(String ref) {
            String[] parts = ref.split(":");
            String name = parts.length >= 2 ? parts[0] + ":" + parts[1] : ref;
            String version = parts.length >= 4 ? parts[3] : "";
            return new ArtifactRef(ref, name, version);
        }
    }
}
