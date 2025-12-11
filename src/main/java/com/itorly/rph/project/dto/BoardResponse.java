package com.itorly.rph.project.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class BoardResponse {

    private Long projectId;
    private String projectName;
    private List<BoardColumnResponse> columns;

    public BoardResponse(Long projectId, String projectName, List<BoardColumnResponse> columns) {
        this.projectId = projectId;
        this.projectName = projectName;
        this.columns = columns;
    }

}
