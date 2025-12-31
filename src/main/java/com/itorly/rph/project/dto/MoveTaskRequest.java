package com.itorly.rph.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoveTaskRequest {

    @NotNull
    @Schema(description = "Target column ID to move the task into", example = "21")
    private Long targetColumnId;

    // In the future we can add newPosition etc.

}
