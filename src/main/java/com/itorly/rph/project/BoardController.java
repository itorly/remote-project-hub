package com.itorly.rph.project;

import com.itorly.rph.project.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}")
public class BoardController {

    private final BoardService boardService;

    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    @GetMapping("/board")
    public ResponseEntity<BoardResponse> getBoard(@PathVariable Long projectId) {
        BoardResponse board = boardService.getBoard(projectId);
        return ResponseEntity.ok(board);
    }

    @GetMapping("/columns/{columnId}")
    public ResponseEntity<BoardColumnResponse> getColumn(
            @PathVariable Long projectId,
            @PathVariable Long columnId
    ) {
        BoardColumnResponse response = boardService.getColumn(projectId, columnId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/columns")
    public ResponseEntity<BoardColumnResponse> createColumn(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateColumnRequest request
    ) {
        BoardColumnResponse response = boardService.createColumn(projectId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/columns/{columnId}")
    public ResponseEntity<BoardColumnResponse> updateColumn(
            @PathVariable Long projectId,
            @PathVariable Long columnId,
            @Valid @RequestBody UpdateColumnRequest request
    ) {
        BoardColumnResponse response = boardService.updateColumn(projectId, columnId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/columns/{columnId}")
    public ResponseEntity<Void> deleteColumn(
            @PathVariable Long projectId,
            @PathVariable Long columnId
    ) {
        boardService.deleteColumn(projectId, columnId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        TaskResponse response = boardService.createTask(projectId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/tasks/{taskId}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request
    ) {
        TaskResponse response = boardService.updateTask(projectId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/tasks/{taskId}/move")
    public ResponseEntity<TaskResponse> moveTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId,
            @Valid @RequestBody MoveTaskRequest request
    ) {
        TaskResponse response = boardService.moveTask(projectId, taskId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(
            @PathVariable Long projectId,
            @PathVariable Long taskId
    ) {
        boardService.deleteTask(projectId, taskId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/activity")
    public ResponseEntity<List<ActivityLogResponse>> getActivity(@PathVariable Long projectId) {
        List<ActivityLogResponse> response = boardService.getActivity(projectId);
        return ResponseEntity.ok(response);
    }
}
