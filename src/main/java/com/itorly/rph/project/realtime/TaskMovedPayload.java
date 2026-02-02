package com.itorly.rph.project.realtime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class TaskMovedPayload {

    @Schema(description = "Moved task ID", example = "101")
    private final Long taskId;
    @Schema(description = "Column ID before the move", example = "10")
    private final Long fromColumnId;
    @Schema(description = "Column ID after the move", example = "11")
    private final Long toColumnId;
    @Schema(description = "Position before the move", example = "0")
    private final Integer fromPosition;
    @Schema(description = "Position after the move", example = "3")
    private final Integer toPosition;

    public TaskMovedPayload(Long taskId, Long fromColumnId, Long toColumnId, Integer fromPosition, Integer toPosition) {
        this.taskId = taskId;
        this.fromColumnId = fromColumnId;
        this.toColumnId = toColumnId;
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
    }
}
