package com.itorly.rph.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateTaskRequest {

    @Size(min = 2, max = 255)
    @Schema(description = "Updated task title", example = "Finalize landing page designs")
    private String title;

    @Size(max = 4000)
    @Schema(description = "Updated description for the task", example = "Incorporate stakeholder feedback and prepare assets")
    private String description;

    @Schema(description = "New assignee's user ID", example = "22")
    private Long assigneeId; // optional

    @Schema(description = "Whether to clear the current assignee", example = "true")
    private Boolean clearAssignee; // explicitly remove assignee

    @Schema(description = "Updated due date in ISO-8601 UTC format", example = "2024-11-15T12:00:00Z")
    private Instant dueDate; // optional, in UTC

    @Schema(description = "Whether to remove the due date", example = "true")
    private Boolean clearDueDate; // explicitly remove due date

    @Schema(description = "Updated comma-separated list of tags", example = "ui,ux")
    private String tags; // comma-separated, optional

    @Schema(description = "Whether to clear the tags", example = "true")
    private Boolean clearTags; // explicitly remove tags
}
