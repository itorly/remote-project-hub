package com.itorly.rph.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorly.rph.project.dto.*;
import com.itorly.rph.security.CustomUserDetailsService;
import com.itorly.rph.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = BoardController.class,
        excludeAutoConfiguration = { SecurityAutoConfiguration.class }
)
@AutoConfigureMockMvc(addFilters = false)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BoardService boardService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void getBoard_returnsBoardResponse() throws Exception {
        Long projectId = 5L;

        TaskResponse task = new TaskResponse(
                100L,
                20L,
                "Design homepage",
                "Create initial mockups",
                TaskStatus.IN_PROGRESS,
                99L,
                "Alex Doe",
                Instant.parse("2024-09-01T00:00:00Z"),
                "ui,ux"
        );

        BoardColumnResponse column = new BoardColumnResponse(
                20L,
                "In Progress",
                1,
                List.of(task)
        );

        BoardResponse boardResponse = new BoardResponse(projectId, "Website", List.of(column));

        when(boardService.getBoard(projectId)).thenReturn(boardResponse);

        mockMvc.perform(get("/api/projects/{projectId}/board", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.projectId").value(projectId))
                .andExpect(jsonPath("$.projectName").value("Website"))
                .andExpect(jsonPath("$.columns[0].id").value(20L))
                .andExpect(jsonPath("$.columns[0].tasks[0].title").value("Design homepage"))
                .andExpect(jsonPath("$.columns[0].tasks[0].assigneeDisplayName").value("Alex Doe"));
    }

    @Test
    void getColumn_returnsColumnResponse() throws Exception {
        Long projectId = 5L;
        Long columnId = 20L;

        BoardColumnResponse columnResponse = new BoardColumnResponse(
                columnId,
                "In Progress",
                1,
                List.of()
        );

        when(boardService.getColumn(projectId, columnId)).thenReturn(columnResponse);

        mockMvc.perform(get("/api/projects/{projectId}/columns/{columnId}", projectId, columnId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(columnId))
                .andExpect(jsonPath("$.name").value("In Progress"))
                .andExpect(jsonPath("$.position").value(1));
    }

    @Test
    void createColumn_returnsColumnResponse() throws Exception {
        Long projectId = 5L;

        CreateColumnRequest request = new CreateColumnRequest();
        request.setName("Done");
        request.setPosition(2);

        BoardColumnResponse columnResponse = new BoardColumnResponse(30L, "Done", 2, List.of());

        when(boardService.createColumn(eq(projectId), any(CreateColumnRequest.class)))
                .thenReturn(columnResponse);

        mockMvc.perform(post("/api/projects/{projectId}/columns", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(30L))
                .andExpect(jsonPath("$.name").value("Done"))
                .andExpect(jsonPath("$.position").value(2));
    }

    @Test
    void updateColumn_returnsUpdatedResponse() throws Exception {
        Long projectId = 5L;
        Long columnId = 20L;

        UpdateColumnRequest request = new UpdateColumnRequest();
        request.setName("QA");
        request.setPosition(3);

        BoardColumnResponse columnResponse = new BoardColumnResponse(columnId, "QA", 3, List.of());

        when(boardService.updateColumn(eq(projectId), eq(columnId), any(UpdateColumnRequest.class)))
                .thenReturn(columnResponse);

        mockMvc.perform(patch("/api/projects/{projectId}/columns/{columnId}", projectId, columnId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(columnId))
                .andExpect(jsonPath("$.name").value("QA"))
                .andExpect(jsonPath("$.position").value(3));
    }

    @Test
    void deleteColumn_returnsNoContent() throws Exception {
        Long projectId = 5L;
        Long columnId = 20L;

        mockMvc.perform(delete("/api/projects/{projectId}/columns/{columnId}", projectId, columnId))
                .andExpect(status().isNoContent());
    }

    @Test
    void createTask_returnsTaskResponse() throws Exception {
        Long projectId = 5L;

        CreateTaskRequest request = new CreateTaskRequest();
        request.setColumnId(20L);
        request.setTitle("Implement API");
        request.setDescription("Build activity log endpoint");
        request.setAssigneeId(99L);
        request.setDueDate(Instant.parse("2024-09-15T00:00:00Z"));
        request.setTags("backend,activity");

        TaskResponse taskResponse = new TaskResponse(
                101L,
                20L,
                "Implement API",
                "Build activity log endpoint",
                TaskStatus.TODO,
                99L,
                "Alex Doe",
                Instant.parse("2024-09-15T00:00:00Z"),
                "backend,activity"
        );

        when(boardService.createTask(eq(projectId), any(CreateTaskRequest.class)))
                .thenReturn(taskResponse);

        mockMvc.perform(post("/api/projects/{projectId}/tasks", projectId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(101L))
                .andExpect(jsonPath("$.columnId").value(20L))
                .andExpect(jsonPath("$.title").value("Implement API"))
                .andExpect(jsonPath("$.assigneeDisplayName").value("Alex Doe"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void moveTask_returnsUpdatedTaskResponse() throws Exception {
        Long projectId = 5L;
        Long taskId = 101L;

        MoveTaskRequest request = new MoveTaskRequest();
        request.setTargetColumnId(50L);

        TaskResponse updatedTask = new TaskResponse(
                taskId,
                50L,
                "Implement API",
                "Build activity log endpoint",
                TaskStatus.IN_PROGRESS,
                99L,
                "Alex Doe",
                Instant.parse("2024-09-15T00:00:00Z"),
                "backend,activity"
        );

        when(boardService.moveTask(eq(projectId), eq(taskId), any(MoveTaskRequest.class)))
                .thenReturn(updatedTask);

        mockMvc.perform(patch("/api/projects/{projectId}/tasks/{taskId}/move", projectId, taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.columnId").value(50L))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void getActivity_returnsActivityLog() throws Exception {
        Long projectId = 5L;

        ActivityLogResponse logResponse = new ActivityLogResponse(
                200L,
                projectId,
                101L,
                "Implement API",
                ActivityActionType.TASK_MOVED,
                "In Progress",
                "Done",
                300L,
                "Jordan Smith",
                Instant.parse("2024-08-01T10:00:00Z")
        );

        when(boardService.getActivity(projectId)).thenReturn(List.of(logResponse));

        mockMvc.perform(get("/api/projects/{projectId}/activity", projectId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(200L))
                .andExpect(jsonPath("$[0].taskTitle").value("Implement API"))
                .andExpect(jsonPath("$[0].actionType").value("TASK_MOVED"))
                .andExpect(jsonPath("$[0].actorDisplayName").value("Jordan Smith"));
    }
}
