package com.itorly.rph.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.List;

@Getter
public class BoardColumnResponse {

    @Schema(description = "Column ID", example = "11")
    private Long id;
    @Schema(description = "Column name", example = "In Progress")
    private String name;
    @Schema(description = "Column position in the board", example = "2")
    private Integer position;
    @Schema(description = "Tasks in the column")
    private List<TaskResponse> tasks;

    public BoardColumnResponse(Long id, String name, Integer position, List<TaskResponse> tasks) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.tasks = tasks;
    }

}
