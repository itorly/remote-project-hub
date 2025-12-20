package com.itorly.rph.project;

import com.itorly.rph.project.dto.CreateProjectRequest;
import com.itorly.rph.project.dto.ProjectResponse;
import com.itorly.rph.project.dto.UpdateProjectRequest;
import com.itorly.rph.security.CustomUserDetailsService;
import com.itorly.rph.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = ProjectController.class,
        excludeAutoConfiguration = { SecurityAutoConfiguration.class }
)
@AutoConfigureMockMvc(addFilters = false) // do not run security filters in MockMvc
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Our controller's dependency
    @MockitoBean
    private ProjectService projectService;

    // Security dependencies needed to construct JwtAuthenticationFilter
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void createProject_returnsProjectResponse() throws Exception {
        Long orgId = 1L;

        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("First Project");
        request.setDescription("My first project");

        ProjectResponse response = new ProjectResponse(
                10L,
                "First Project",
                "My first project",
                ProjectStatus.ACTIVE,
                orgId
        );

        when(projectService.createProject(eq(orgId), any(CreateProjectRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/organizations/{organizationId}/projects", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("First Project"))
                .andExpect(jsonPath("$.description").value("My first project"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.organizationId").value(orgId));
    }

    @Test
    void getProjects_returnsProjectList() throws Exception {
        Long orgId = 1L;

        ProjectResponse p1 = new ProjectResponse(
                10L,
                "First Project",
                "My first project",
                ProjectStatus.ACTIVE,
                orgId
        );
        ProjectResponse p2 = new ProjectResponse(
                11L,
                "Second Project",
                "Second project",
                ProjectStatus.ACTIVE,
                orgId
        );

        when(projectService.getProjectsForOrganization(orgId))
                .thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/api/organizations/{organizationId}/projects", orgId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // first element
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].name").value("First Project"))
                .andExpect(jsonPath("$[0].organizationId").value(orgId))
                // second element
                .andExpect(jsonPath("$[1].id").value(11L))
                .andExpect(jsonPath("$[1].name").value("Second Project"))
                .andExpect(jsonPath("$[1].organizationId").value(orgId));
    }

    @Test
    void updateProject_returnsUpdatedProject() throws Exception {
        Long orgId = 1L;
        Long projectId = 10L;

        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("Updated Project");
        request.setDescription("Updated description");
        request.setStatus(ProjectStatus.ARCHIVED);

        ProjectResponse response = new ProjectResponse(
                projectId,
                "Updated Project",
                "Updated description",
                ProjectStatus.ARCHIVED,
                orgId
        );

        when(projectService.updateProject(eq(orgId), eq(projectId), any(UpdateProjectRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/organizations/{organizationId}/projects/{projectId}", orgId, projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.name").value("Updated Project"))
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    @Test
    void deleteProject_returnsNoContent() throws Exception {
        Long orgId = 1L;
        Long projectId = 10L;

        mockMvc.perform(delete("/api/organizations/{organizationId}/projects/{projectId}", orgId, projectId))
                .andExpect(status().isNoContent());
    }
}
