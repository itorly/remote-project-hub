package com.itorly.rph.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class AuthResponse {

    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    private String accessToken;

    @Schema(description = "Refresh token", example = "r1YtX2p0...")
    private String refreshToken;

    @Schema(description = "Authenticated user's ID", example = "5")
    private Long userId;

    @Schema(description = "Authenticated user's email", example = "user@example.com")
    private String email;

    @Schema(description = "Authenticated user's display name", example = "Jordan")
    private String displayName;

    public AuthResponse(String accessToken, String refreshToken, Long userId, String email, String displayName) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.email = email;
        this.displayName = displayName;
    }
}
