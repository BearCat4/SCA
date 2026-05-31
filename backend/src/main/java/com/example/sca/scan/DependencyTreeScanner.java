package com.example.sca.scan;

import java.nio.file.Path;
import java.util.List;

public interface DependencyTreeScanner {
    List<DependencyEdge> scan(Path directory);
}
