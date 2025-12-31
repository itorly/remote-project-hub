package com.itorly.rph.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
public class BoardResponse {

    @Schema(description = "ID of the project the board belongs to", example = "7")
    private Long projectId;
    @Schema(description = "Project name", example = "Mobile app revamp")
    private String projectName;
    @Schema(description = "Columns on the board")
    private List<BoardColumnResponse> columns;

    public BoardResponse(Long projectId, String projectName, List<BoardColumnResponse> columns) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.columns = columns;
    }

}
