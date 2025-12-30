package com.itorly.rph.project;

import com.itorly.rph.project.dto.CreateProjectRequest;
import com.itorly.rph.project.dto.ProjectResponse;
import com.itorly.rph.project.dto.UpdateProjectRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations/{organizationId}/projects")
@Tag(name = "Project Management", description = "Manage projects within an organization")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @PostMapping
    @Operation(summary = "Create project", description = "Create a new project under the specified organization")
    public ResponseEntity<ProjectResponse> createProject(
            @PathVariable Long organizationId,
            @Valid @RequestBody CreateProjectRequest request
    ) {
        ProjectResponse response = projectService.createProject(organizationId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List projects", description = "Get all projects for the specified organization")
    public ResponseEntity<List<ProjectResponse>> getProjects(
            @PathVariable Long organizationId
    ) {
        List<ProjectResponse> projects = projectService.getProjectsForOrganization(organizationId);
        return ResponseEntity.ok(projects);
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "Update project", description = "Update an existing project in the specified organization")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long organizationId,
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request
    ) {
        ProjectResponse response = projectService.updateProject(organizationId, projectId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Delete project", description = "Delete a project from the specified organization")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long organizationId,
            @PathVariable Long projectId
    ) {
        projectService.deleteProject(organizationId, projectId);
        return ResponseEntity.noContent().build();
    }
}
