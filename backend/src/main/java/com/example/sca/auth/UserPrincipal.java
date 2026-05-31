package com.example.sca.auth;

public class UserPrincipal {
    private final Long id;
    private final String username;
    private final UserRole role;

    public UserPrincipal(Long id, String username, UserRole role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }
}
