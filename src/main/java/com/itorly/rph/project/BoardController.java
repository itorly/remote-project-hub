package com.itorly.rph.project;

import com.itorly.rph.project.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/columns")
    public ResponseEntity<BoardColumnResponse> createColumn(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateColumnRequest request
    ) {
        BoardColumnResponse response = boardService.createColumn(projectId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/tasks")
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request
    ) {
        TaskResponse response = boardService.createTask(projectId, request);
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
}
