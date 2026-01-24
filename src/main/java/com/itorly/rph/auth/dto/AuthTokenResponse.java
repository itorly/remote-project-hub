package com.itorly.rph.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class AuthTokenResponse {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private final String token;

    @Schema(description = "Refresh token (long-lived)", example = "v1UfxEZX2eKZ8Kyo8F3...")
    private final String refreshToken;

    public AuthTokenResponse(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
