package com.itorly.rph.project.dto;

import com.itorly.rph.project.ProjectStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class ProjectResponse {

    @Schema(description = "Project ID", example = "10")
    private Long id;
    @Schema(description = "Project name", example = "Website redesign")
    private String name;
    @Schema(description = "Project description", example = "Complete redesign of the public website")
    private String description;
    @Schema(description = "Project status", example = "ACTIVE")
    private ProjectStatus status;
    @Schema(description = "Owning organization ID", example = "3")
    private Long organizationId;

    public ProjectResponse(Long id, String name, String description,
                           ProjectStatus status, Long organizationId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.organizationId = organizationId;
    }

}
