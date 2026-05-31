package com.example.sca.auth;

import com.example.sca.common.ApiException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TokenService {
    private final String secret;

    public TokenService(@Value("${sca.security.token-secret}") String secret) {
        this.secret = secret;
    }

    public String issueUserToken(AppUser user) {
        String payload = user.getId() + ":" + user.getUsername() + ":" + user.getRole() + ":" + Instant.now().getEpochSecond();
        return encode(payload) + "." + sign(payload);
    }

    public Optional<UserPrincipal> parseUserToken(String token) {
        if (token == null || !token.contains(".")) {
            return Optional.empty();
        }
        String[] parts = token.split("\\.", 2);
        String payload = decode(parts[0]);
        if (payload == null || !sign(payload).equals(parts[1])) {
            return Optional.empty();
        }
        String[] fields = payload.split(":", 4);
        if (fields.length < 3) {
            return Optional.empty();
        }
        try {
            return Optional.of(new UserPrincipal(Long.parseLong(fields[0]), fields[1], UserRole.valueOf(fields[2])));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    public String newProjectToken() {
        String payload = "project:" + Instant.now().toEpochMilli() + ":" + Math.random();
        return "sca_" + encode(payload) + "_" + sign(payload).substring(0, 24);
    }

    public String hashProjectToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Project token is required");
        }
        return sign(token);
    }

    private String encode(String value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }

    private String decode(String value) {
        try {
            return new String(Base64.getUrlDecoder().decode(value), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return encodeBytes(mac.doFinal(payload.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot sign token", ex);
        }
    }

    private String encodeBytes(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
