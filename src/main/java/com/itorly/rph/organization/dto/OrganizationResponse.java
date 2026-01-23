package com.itorly.rph.organization.dto;

import com.itorly.rph.organization.OrganizationRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class OrganizationResponse {

    @Schema(description = "Organization ID", example = "12")
    private Long id;

    @Schema(description = "Organization name", example = "Acme Corporation")
    private String name;

    @Schema(description = "Organization description", example = "Primary organization for enterprise projects")
    private String description;

    @Schema(description = "Role of the current user in the organization", example = "OWNER")
    private OrganizationRole role;

    public OrganizationResponse(Long id, String name, String description, OrganizationRole role) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.role = role;
    }

}
