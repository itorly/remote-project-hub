package com.itorly.rph.organization.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateOrganizationRequest {

    @NotBlank
    @Size(min = 2, max = 255)
    @Schema(description = "Updated organization name", example = "Acme Corp")
    private String name;

    @Size(max = 2000)
    @Schema(description = "Updated organization description", example = "Now focusing on remote collaboration tools")
    private String description;
}
