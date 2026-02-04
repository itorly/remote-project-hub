package com.itorly.rph.project.realtime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class TaskDeletedPayload {

    @Schema(description = "Deleted task ID", example = "101")
    private final Long taskId;
    @Schema(description = "Column ID where the task lived", example = "10")
    private final Long columnId;

    public TaskDeletedPayload(Long taskId, Long columnId) {
        this.taskId = taskId;
        this.columnId = columnId;
    }
}
