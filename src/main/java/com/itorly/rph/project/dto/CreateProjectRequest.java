package com.itorly.rph.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProjectRequest {

    @NotBlank
    @Size(min = 2, max = 255)
    @Schema(description = "Name of the project", example = "Website redesign")
    private String name;

    @Size(max = 2000)
    @Schema(description = "Optional project description", example = "Redesign the corporate website by Q3")
    private String description;

}
