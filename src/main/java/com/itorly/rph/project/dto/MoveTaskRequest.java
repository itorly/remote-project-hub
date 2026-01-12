package com.itorly.rph.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoveTaskRequest {

    @NotNull
    @Schema(description = "Target column ID to move the task into", example = "21")
    private Long targetColumnId;

    @NotNull
    @Schema(description = "Original position of the task in the source column", example = "0")
    private Integer fromPosition;

    @NotNull
    @Schema(description = "New position of the task in the target column", example = "3")
    private Integer toPosition;

}
