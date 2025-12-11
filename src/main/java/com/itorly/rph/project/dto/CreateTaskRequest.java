package com.itorly.rph.project.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateTaskRequest {

    @NotNull
    private Long columnId;

    @NotBlank
    @Size(min = 2, max = 255)
    private String title;

    @Size(max = 4000)
    private String description;

    private Long assigneeId; // optional

    private Instant dueDate; // optional, in UTC

    private String tags; // comma-separated, optional

}
