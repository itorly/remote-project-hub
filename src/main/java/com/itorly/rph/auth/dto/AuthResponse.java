package com.itorly.rph.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class AuthResponse {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String token;
    @Schema(description = "Authenticated user's ID", example = "5")
    private Long userId;
    @Schema(description = "Authenticated user's email", example = "user@example.com")
    private String email;
    @Schema(description = "Authenticated user's display name", example = "Jordan")
    private String displayName;

    public AuthResponse(String token, Long userId, String email, String displayName) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
    }

    // getters
}
