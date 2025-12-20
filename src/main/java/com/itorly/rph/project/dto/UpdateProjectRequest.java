package com.itorly.rph.project.dto;

import com.itorly.rph.project.ProjectStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProjectRequest {

    @NotBlank
    @Size(min = 2, max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    @NotNull
    private ProjectStatus status;
}
