package com.itorly.rph.project.dto;

import com.itorly.rph.project.TaskStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;

@Getter
public class TaskResponse {

    @Schema(description = "Task ID", example = "101")
    private Long id;
    @Schema(description = "ID of the column containing the task", example = "11")
    private Long columnId;
    @Schema(description = "Task title", example = "Design header component")
    private String title;
    @Schema(description = "Task description", example = "Create reusable header component in design system")
    private String description;
    @Schema(description = "Task status", example = "OPEN")
    private TaskStatus status;
    @Schema(description = "Assignee user ID", example = "25")
    private Long assigneeId;
    @Schema(description = "Assignee display name", example = "Jordan Smith")
    private String assigneeDisplayName;
    @Schema(description = "Task due date in ISO-8601 UTC format", example = "2024-10-01T17:00:00Z")
    private Instant dueDate;
    @Schema(description = "Comma-separated tags for the task", example = "frontend,design")
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
