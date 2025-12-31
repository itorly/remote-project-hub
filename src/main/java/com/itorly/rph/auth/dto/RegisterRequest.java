package com.itorly.rph.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class RegisterRequest {

    @Email
    @NotBlank
    @Schema(description = "User email address", example = "newuser@example.com")
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
    @Schema(description = "Account password", example = "P@ssw0rd!")
    private String password;

    @NotBlank
    @Size(min = 2, max = 100)
    @Schema(description = "Display name for the user", example = "Taylor Swift")
    private String displayName;

    @Schema(description = "User timezone (IANA identifier)", example = "America/New_York")
    private String timezone;

    // getters/setters
}
