package com.itorly.rph.project.dto;

import com.itorly.rph.project.ActivityActionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ActivityLogResponse {

    @Schema(description = "Activity log entry ID", example = "301")
    private Long id;
    @Schema(description = "Associated project ID", example = "7")
    private Long projectId;
    @Schema(description = "Associated task ID", example = "101")
    private Long taskId;
    @Schema(description = "Title of the related task", example = "Design header component")
    private String taskTitle;
    @Schema(description = "Type of action performed", example = "UPDATED")
    private ActivityActionType actionType;
    @Schema(description = "Previous value (if applicable)", example = "To Do")
    private String oldValue;
    @Schema(description = "New value (if applicable)", example = "In Progress")
    private String newValue;
    @Schema(description = "ID of the user who performed the action", example = "25")
    private Long actorId;
    @Schema(description = "Display name of the user who performed the action", example = "Jordan Smith")
    private String actorDisplayName;
    @Schema(description = "Timestamp of the activity in ISO-8601 UTC format", example = "2024-09-10T10:15:30Z")
    private Instant createdAt;

    public ActivityLogResponse(Long id, Long projectId, Long taskId, String taskTitle,
                               ActivityActionType actionType, String oldValue, String newValue,
                               Long actorId, String actorDisplayName, Instant createdAt) {
        this.id = id;
        this.projectId = projectId;
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.actionType = actionType;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.actorId = actorId;
        this.actorDisplayName = actorDisplayName;
        this.createdAt = createdAt;
    }
}
