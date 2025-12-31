package com.itorly.rph.project.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateTaskRequest {

    @NotNull
    @Schema(description = "ID of the column the task belongs to", example = "42")
    private Long columnId;

    @NotBlank
    @Size(min = 2, max = 255)
    @Schema(description = "Task title", example = "Design landing page")
    private String title;

    @Size(max = 4000)
    @Schema(description = "Detailed task description", example = "Create wireframes and high-fidelity mockups")
    private String description;

    @Schema(description = "User ID of the assignee (optional)", example = "15")
    private Long assigneeId; // optional

    @Schema(description = "Due date in ISO-8601 UTC format", example = "2024-12-31T23:59:59Z")
    private Instant dueDate; // optional, in UTC

    @Schema(description = "Comma-separated list of tags", example = "design,frontend,q3")
    private String tags; // comma-separated, optional

}
