package com.example.sca.trivy;

public class TrivyException extends RuntimeException {
    public TrivyException(String message) {
        super(message);
    }

    public TrivyException(String message, Throwable cause) {
        super(message, cause);
    }
}
