package com.itorly.rph.project.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateTaskRequest {

    @Size(min = 2, max = 255)
    private String title;

    @Size(max = 4000)
    private String description;

    private Long assigneeId; // optional

    private Boolean clearAssignee; // explicitly remove assignee

    private Instant dueDate; // optional, in UTC

    private Boolean clearDueDate; // explicitly remove due date

    private String tags; // comma-separated, optional

    private Boolean clearTags; // explicitly remove tags
}
