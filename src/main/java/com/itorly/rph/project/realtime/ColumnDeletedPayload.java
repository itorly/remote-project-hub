package com.itorly.rph.project.realtime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class ColumnDeletedPayload {

    @Schema(description = "Deleted column ID", example = "12")
    private final Long columnId;
    @Schema(description = "Position of the column before deletion", example = "2")
    private final Integer position;

    public ColumnDeletedPayload(Long columnId, Integer position) {
        this.columnId = columnId;
        this.position = position;
    }
}
