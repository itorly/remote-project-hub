package com.itorly.rph.project;

import com.itorly.rph.project.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/projects/{projectId}")
@Tag(name = "Project Board", description = "Endpoints for managing project boards, columns, and tasks.")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/board")
    @Operation(summary = "Get board", description = "Retrieve the board for the specified project.")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable Long projectId) {
        BoardResponse board = boardService.getBoard(projectId);
        return ResponseEntity.ok(board);
    }

    @GetMapping("/columns/{columnId}")
    @Operation(summary = "Get column", description = "Fetch a specific column within a project board.")
    public ResponseEntity<BoardColumnResponse> getColumn(
            @PathVariable Long projectId,
            @PathVariable Long columnId
    ) {
        BoardColumnResponse response = boardService.getColumn(projectId, columnId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/columns")
    @Operation(summary = "Create column", description = "Add a new column to the project board.")
    public ResponseEntity<BoardColumnResponse> createColumn(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateColumnRequest request
    ) {
        BoardColumnResponse response = boardService.createColumn(projectId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/columns/{columnId}")
    @Operation(summary = "Update column", description = "Update the name or order of a board column.")
    public ResponseEntity<BoardColumnResponse> updateColumn(
            @PathVariable Long projectId,
            @PathVariable Long columnId,
            @Valid @RequestBody UpdateColumnRequest request
    ) {
        BoardColumnResponse response = boardService.updateColumn(projectId, columnId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/columns/{columnId}")
    @Operation(summary = "Delete column", description = "Remove a column from the project board.")
    public ResponseEntity<Void> deleteColumn(
            @PathVariable Long projectId,
            @PathVariable Long columnId
    ) {
        boardService.deleteColumn(projectId, columnId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tasks")
    @Operation(summary = "Create task", description = "Create a new task in the specified project.")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        TaskResponse response = boardService.createTask(projectId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/tasks/{taskId}")
    @Operation(summary = "Update task", description = "Modify task details for the specified task ID.")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        TaskResponse response = boardService.updateTask(projectId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/tasks/{taskId}/move")
    @Operation(summary = "Move task", description = "Move a task to a different column or position.")
    public ResponseEntity<TaskResponse> moveTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody MoveTaskRequest request
    ) {
        TaskResponse response = boardService.moveTask(projectId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/tasks/{taskId}")
    @Operation(summary = "Delete task", description = "Delete a task from the project.")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        boardService.deleteTask(projectId, taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/activity")
    @Operation(summary = "Get project activity", description = "Retrieve recent activity logs for the project.")
    public ResponseEntity<List<ActivityLogResponse>> getActivity(@PathVariable Long projectId) {
        List<ActivityLogResponse> response = boardService.getActivity(projectId);
        return ResponseEntity.ok(response);
    }
}
