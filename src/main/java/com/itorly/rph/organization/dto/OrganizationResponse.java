package com.itorly.rph.organization.dto;

import com.itorly.rph.organization.OrganizationRole;
import lombok.Getter;

@Getter
public class OrganizationResponse {

    private Long id;
    private String name;
    private String description;
    private OrganizationRole role;

    public OrganizationResponse(Long id, String name, String description, OrganizationRole role) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.role = role;
    }

}
