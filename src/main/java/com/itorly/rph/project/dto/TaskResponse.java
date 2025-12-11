package com.itorly.rph.project.dto;

import com.itorly.rph.project.TaskStatus;
import lombok.Getter;

import java.time.Instant;

@Getter
public class TaskResponse {

    private Long id;
    private Long columnId;
    private String title;
    private String description;
    private TaskStatus status;
    private Long assigneeId;
    private String assigneeDisplayName;
    private Instant dueDate;
    private String tags;

    public TaskResponse(Long id, Long columnId, String title, String description,
                        TaskStatus status, Long assigneeId, String assigneeDisplayName,
                        Instant dueDate, String tags) {
        this.id = id;
        this.columnId = columnId;
        this.title = title;
        this.description = description;
        this.status = status;
        this.assigneeId = assigneeId;
        this.assigneeDisplayName = assigneeDisplayName;
        this.dueDate = dueDate;
        this.tags = tags;
    }

}
