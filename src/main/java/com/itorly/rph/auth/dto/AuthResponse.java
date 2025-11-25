package com.itorly.rph.auth.dto;

import lombok.Getter;

@Getter
public class AuthResponse {

    private String token;
    private Long userId;
    private String email;
    private String displayName;

    public AuthResponse(String token, Long userId, String email, String displayName) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
    }

    // getters
}
