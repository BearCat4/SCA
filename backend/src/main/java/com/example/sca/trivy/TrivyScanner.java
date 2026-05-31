package com.example.sca.trivy;

import java.nio.file.Path;

public interface TrivyScanner {
    String scan(Path directory);
    String scanImage(String imageRef);
}
