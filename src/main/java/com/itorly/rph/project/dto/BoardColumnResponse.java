package com.itorly.rph.project.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class BoardColumnResponse {

    private Long id;
    private String name;
    private Integer position;
    private List<TaskResponse> tasks;

    public BoardColumnResponse(Long id, String name, Integer position, List<TaskResponse> tasks) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.tasks = tasks;
    }

}
