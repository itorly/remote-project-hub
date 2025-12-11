package com.itorly.rph.project.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MoveTaskRequest {

    @NotNull
    private Long targetColumnId;

    // In the future we can add newPosition etc.

}
