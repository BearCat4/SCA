package com.example.sca.scan;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.stereotype.Component;

@Component
public class SourcePathResolver {
    public Path localDirectoryOrNull(String source) {
        if (source == null || source.trim().isEmpty()) {
            return null;
        }
        String value = source.trim();
        Path path;
        if (value.startsWith("file://")) {
            path = Paths.get(URI.create(value));
        } else {
            path = Paths.get(value);
        }
        return Files.exists(path) ? path.toAbsolutePath().normalize() : null;
    }
}
