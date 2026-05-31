package com.example.sca.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TokenServiceTest {
    private final TokenService tokenService = new TokenService("unit-test-secret");

    @Test
    void issuesAndParsesUserToken() {
        AppUser user = new AppUser();
        user.setId(42L);
        user.setUsername("admin");
        user.setRole(UserRole.ADMIN);

        String token = tokenService.issueUserToken(user);

        assertThat(tokenService.parseUserToken(token)).isPresent();
        assertThat(tokenService.parseUserToken(token).get().getUsername()).isEqualTo("admin");
    }

    @Test
    void rejectsTamperedUserToken() {
        AppUser user = new AppUser();
        user.setId(42L);
        user.setUsername("admin");
        user.setRole(UserRole.ADMIN);

        String token = tokenService.issueUserToken(user) + "x";

        assertThat(tokenService.parseUserToken(token)).isEmpty();
    }

    @Test
    void projectTokenHashIsStable() {
        String token = tokenService.newProjectToken();

        assertThat(tokenService.hashProjectToken(token)).isEqualTo(tokenService.hashProjectToken(token));
        assertThat(token).startsWith("sca_");
    }
}
