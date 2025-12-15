package com.itorly.rph.project.dto;

import com.itorly.rph.project.ActivityActionType;
import lombok.Getter;

import java.time.Instant;

@Getter
public class ActivityLogResponse {

    private Long id;
    private Long projectId;
    private Long taskId;
    private String taskTitle;
    private ActivityActionType actionType;
    private String oldValue;
    private String newValue;
    private Long actorId;
    private String actorDisplayName;
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
