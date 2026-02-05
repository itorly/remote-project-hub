package com.itorly.rph.organization.dto;

import com.itorly.rph.organization.OrganizationRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateOrganizationMemberRoleRequest {

    @NotNull
    @Schema(description = "New role for the member", example = "ADMIN")
    private OrganizationRole role;
}
