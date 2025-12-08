package com.itorly.rph.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateProjectRequest {

    @NotBlank
    @Size(min = 2, max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

}
