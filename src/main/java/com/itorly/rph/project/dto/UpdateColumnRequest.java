package com.itorly.rph.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateColumnRequest {

    @Size(min = 2, max = 100)
    @Schema(description = "Updated column name", example = "In Progress")
    private String name;

    @Schema(description = "New position for the column", example = "2")
    private Integer position;
}
