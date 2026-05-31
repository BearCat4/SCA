package com.example.sca.scan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LockfileDependencyParser {
    private final ObjectMapper objectMapper;

    public LockfileDependencyParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<DependencyEdge> parse(Path directory) {
        List<DependencyEdge> edges = new ArrayList<DependencyEdge>();
        if (directory == null || !Files.exists(directory)) {
            return edges;
        }
        try {
            Files.walk(directory, 5)
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.toString().contains("/node_modules/"))
                    .forEach(path -> parseFile(directory, path, edges));
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to read dependency lockfiles", ex);
        }
        return edges;
    }

    private void parseFile(Path root, Path file, List<DependencyEdge> edges) {
        String name = file.getFileName().toString();
        try {
            if ("package-lock.json".equals(name)) {
                parsePackageLock(file, edges);
            } else if ("requirements.txt".equals(name)) {
                parseRequirements(root, file, edges);
            } else if ("poetry.lock".equals(name)) {
                parsePoetryLock(file, edges);
            } else if ("gradle.lockfile".equals(name)) {
                parseGradleLockfile(root, file, edges);
            } else if ("go.mod".equals(name)) {
                parseGoMod(file, edges);
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to parse dependency file " + file, ex);
        }
    }

    private void parsePackageLock(Path file, List<DependencyEdge> edges) throws IOException {
        JsonNode root = objectMapper.readTree(new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
        JsonNode packages = root.path("packages");
        if (!packages.isObject()) {
            return;
        }
        Map<String, PackageRef> byPath = new HashMap<String, PackageRef>();
        Iterator<Map.Entry<String, JsonNode>> fields = packages.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String path = field.getKey();
            JsonNode node = field.getValue();
            String packageName = text(node, "name");
            if (packageName.isEmpty()) {
                packageName = packageNameFromNodeModulesPath(path);
            }
            if (packageName.isEmpty() && path.isEmpty()) {
                packageName = text(root, "name");
            }
            if (!packageName.isEmpty()) {
                byPath.put(path, new PackageRef(packageName, text(node, "version")));
            }
        }
        Iterator<Map.Entry<String, JsonNode>> dependencyFields = packages.fields();
        while (dependencyFields.hasNext()) {
            Map.Entry<String, JsonNode> field = dependencyFields.next();
            PackageRef source = byPath.get(field.getKey());
            JsonNode dependencies = field.getValue().path("dependencies");
            if (source == null || !dependencies.isObject()) {
                continue;
            }
            Iterator<Map.Entry<String, JsonNode>> deps = dependencies.fields();
            while (deps.hasNext()) {
                Map.Entry<String, JsonNode> dep = deps.next();
                PackageRef target = findNpmTarget(byPath, field.getKey(), dep.getKey(), dep.getValue().asText(""));
                edges.add(edge(source.name, source.version, npmRef(source.name, source.version),
                        target.name, target.version, npmRef(target.name, target.version), "npm"));
            }
        }
    }

    private PackageRef findNpmTarget(Map<String, PackageRef> byPath, String sourcePath, String name, String requestedVersion) {
        PackageRef direct = byPath.get(sourcePath.isEmpty() ? "node_modules/" + name : sourcePath + "/node_modules/" + name);
        if (direct != null) {
            return direct;
        }
        PackageRef hoisted = byPath.get("node_modules/" + name);
        return hoisted == null ? new PackageRef(name, requestedVersion) : hoisted;
    }

    private String packageNameFromNodeModulesPath(String path) {
        int marker = path.lastIndexOf("node_modules/");
        if (marker < 0) {
            return "";
        }
        return path.substring(marker + "node_modules/".length());
    }

    private void parseRequirements(Path root, Path file, List<DependencyEdge> edges) throws IOException {
        String source = root.relativize(file).toString();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String trimmed = stripComment(line).trim();
            if (trimmed.isEmpty() || trimmed.startsWith("-")) {
                continue;
            }
            PythonRequirement requirement = parseRequirement(trimmed);
            if (!requirement.name.isEmpty()) {
                edges.add(edge(source, "", "python:" + source,
                        requirement.name, requirement.version, "pypi:" + requirement.name + requirement.version, "python"));
            }
        }
    }

    private PythonRequirement parseRequirement(String value) {
        String[] operators = new String[]{"==", ">=", "<=", "~=", "!=", ">", "<"};
        for (String operator : operators) {
            int index = value.indexOf(operator);
            if (index > 0) {
                String version = value.substring(index + operator.length()).trim();
                return new PythonRequirement(value.substring(0, index).trim(),
                        "==".equals(operator) ? version : operator + version);
            }
        }
        return new PythonRequirement(value.trim(), "");
    }

    private void parseGoMod(Path file, List<DependencyEdge> edges) throws IOException {
        String module = file.getParent() == null ? "go.mod" : file.getParent().getFileName().toString();
        boolean readingRequireBlock = false;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if (trimmed.startsWith("module ")) {
                module = trimmed.substring("module ".length()).trim();
            } else if ("require (".equals(trimmed)) {
                readingRequireBlock = true;
            } else if (readingRequireBlock && ")".equals(trimmed)) {
                readingRequireBlock = false;
            } else if (readingRequireBlock) {
                addGoDependency(module, trimmed, edges);
            } else if (trimmed.startsWith("require ")) {
                addGoDependency(module, trimmed.substring("require ".length()).trim(), edges);
            }
        }
    }

    private void addGoDependency(String module, String line, List<DependencyEdge> edges) {
        String trimmed = stripComment(line).trim();
        if (trimmed.isEmpty()) {
            return;
        }
        String scope = line.contains("// indirect") ? "go:indirect" : "go";
        String[] parts = trimmed.split("\\s+");
        if (parts.length < 2) {
            return;
        }
        edges.add(edge(module, "", "go:" + module,
                parts[0], parts[1], "go:" + parts[0] + "@" + parts[1], scope));
    }

    private void parseGradleLockfile(Path root, Path file, List<DependencyEdge> edges) throws IOException {
        String source = root.relativize(file).toString();
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String trimmed = stripComment(line).trim();
            int equals = trimmed.indexOf('=');
            String coordinate = equals >= 0 ? trimmed.substring(0, equals) : trimmed;
            String scope = equals >= 0 ? trimmed.substring(equals + 1) : "gradle";
            String[] parts = coordinate.split(":");
            if (parts.length < 3) {
                continue;
            }
            String name = parts[0] + ":" + parts[1];
            String version = parts[2];
            edges.add(edge(source, "", "gradle:" + source,
                    name, version, "gradle:" + name + ":" + version, scope));
        }
    }

    private void parsePoetryLock(Path file, List<DependencyEdge> edges) throws IOException {
        List<PoetryPackage> packages = new ArrayList<PoetryPackage>();
        PoetryPackage current = null;
        boolean readingDependencies = false;
        for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
            String trimmed = line.trim();
            if ("[[package]]".equals(trimmed)) {
                current = new PoetryPackage();
                packages.add(current);
                readingDependencies = false;
            } else if ("[package.dependencies]".equals(trimmed)) {
                readingDependencies = current != null;
            } else if (trimmed.startsWith("[") && !"[package.dependencies]".equals(trimmed)) {
                readingDependencies = false;
            } else if (current != null && trimmed.startsWith("name = ")) {
                current.name = quotedValue(trimmed);
            } else if (current != null && trimmed.startsWith("version = ")) {
                current.version = quotedValue(trimmed);
            } else if (current != null && readingDependencies && trimmed.contains("=")) {
                current.dependencies.add(trimmed.substring(0, trimmed.indexOf('=')).trim());
            }
        }
        Map<String, PoetryPackage> byName = new HashMap<String, PoetryPackage>();
        for (PoetryPackage item : packages) {
            if (!item.name.isEmpty()) {
                byName.put(item.name, item);
            }
        }
        for (PoetryPackage item : packages) {
            for (String dependency : item.dependencies) {
                PoetryPackage target = byName.get(dependency);
                String version = target == null ? "" : target.version;
                edges.add(edge(item.name, item.version, "pypi:" + item.name + item.version,
                        dependency, version, "pypi:" + dependency + version, "poetry"));
            }
        }
    }

    private String stripComment(String value) {
        int index = value.indexOf('#');
        return index >= 0 ? value.substring(0, index) : value;
    }

    private String quotedValue(String line) {
        int first = line.indexOf('"');
        int last = line.lastIndexOf('"');
        return first >= 0 && last > first ? line.substring(first + 1, last) : "";
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isTextual() ? value.asText() : "";
    }

    private String npmRef(String name, String version) {
        return "npm:" + name + (version == null || version.isEmpty() ? "" : "@" + version);
    }

    private DependencyEdge edge(String sourceName, String sourceVersion, String sourceRef,
                                String targetName, String targetVersion, String targetRef,
                                String scope) {
        DependencyEdge edge = new DependencyEdge();
        edge.setSourceName(sourceName);
        edge.setSourceVersion(sourceVersion == null ? "" : sourceVersion);
        edge.setSourceRef(sourceRef);
        edge.setTargetName(targetName);
        edge.setTargetVersion(targetVersion == null ? "" : targetVersion);
        edge.setTargetRef(targetRef);
        edge.setScope(scope);
        return edge;
    }

    private static class PackageRef {
        private final String name;
        private final String version;

        PackageRef(String name, String version) {
            this.name = name;
            this.version = version == null ? "" : version;
        }
    }

    private static class PythonRequirement {
        private final String name;
        private final String version;

        PythonRequirement(String name, String version) {
            this.name = name;
            this.version = version;
        }
    }

    private static class PoetryPackage {
        private String name = "";
        private String version = "";
        private final List<String> dependencies = new ArrayList<String>();
    }
}
