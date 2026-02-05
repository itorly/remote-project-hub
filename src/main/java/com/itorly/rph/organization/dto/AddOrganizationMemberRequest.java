package com.itorly.rph.organization.dto;

import com.itorly.rph.organization.OrganizationRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddOrganizationMemberRequest {

    @Email
    @NotBlank
    @Schema(description = "Email of the user to add", example = "member@example.com")
    private String email;

    @Schema(description = "Role to assign (defaults to MEMBER)", example = "MEMBER")
    private OrganizationRole role;
}
