package com.itorly.rph.project.realtime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;

@Getter
public class BoardEvent {

    @Schema(description = "Event type", example = "TASK_MOVED")
    private final BoardEventType type;
    @Schema(description = "Project ID", example = "7")
    private final Long projectId;
    @Schema(description = "Event payload")
    private final Object payload;
    @Schema(description = "Event timestamp in UTC")
    private final Instant timestamp;

    public BoardEvent(BoardEventType type, Long projectId, Object payload, Instant timestamp) {
        this.type = type;
        this.projectId = projectId;
        this.payload = payload;
        this.timestamp = timestamp;
    }
}
