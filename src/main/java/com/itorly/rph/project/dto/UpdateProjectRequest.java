package com.itorly.rph.project.dto;

import com.itorly.rph.project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProjectRequest {

    @NotBlank
    @Size(min = 2, max = 255)
    @Schema(description = "Updated project name", example = "New marketing site")
    private String name;

    @Size(max = 2000)
    @Schema(description = "Updated project description", example = "Marketing site refresh with new branding")
    private String description;

    @NotNull
    @Schema(description = "Project status", example = "ACTIVE")
    private ProjectStatus status;
}
