package com.itorly.rph.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateColumnRequest {

    @NotBlank
    @Size(min = 2, max = 100)
    @Schema(description = "Column name", example = "To Do")
    private String name;

    @Schema(description = "Position of the column (optional). If null, the column is appended", example = "1")
    private Integer position; // optional; if null, append

}
