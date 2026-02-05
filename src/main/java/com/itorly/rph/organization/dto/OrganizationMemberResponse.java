package com.itorly.rph.organization.dto;

import com.itorly.rph.organization.OrganizationRole;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(name = "OrganizationMemberResponse")
public record OrganizationMemberResponse(
        @Schema(description = "Membership ID", example = "12")
        Long id,
        @Schema(description = "User ID", example = "7")
        Long userId,
        @Schema(description = "User email", example = "member@example.com")
        String email,
        @Schema(description = "User display name", example = "Jane Doe")
        String displayName,
        @Schema(description = "Member role", example = "MEMBER")
        OrganizationRole role,
        @Schema(description = "Joined timestamp", example = "2024-01-01T12:00:00Z")
        Instant joinedAt
) {
}
