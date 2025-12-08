package com.itorly.rph.project.dto;

import com.itorly.rph.project.ProjectStatus;
import lombok.Getter;

@Getter
public class ProjectResponse {

    private Long id;
    private String name;
    private String description;
    private ProjectStatus status;
    private Long organizationId;

    public ProjectResponse(Long id, String name, String description,
                           ProjectStatus status, Long organizationId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.organizationId = organizationId;
    }

}
