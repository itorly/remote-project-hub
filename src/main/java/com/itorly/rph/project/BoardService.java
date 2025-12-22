package com.itorly.rph.project;

import com.itorly.rph.common.exception.BadRequestException;
import com.itorly.rph.common.exception.ForbiddenException;
import com.itorly.rph.common.exception.UnauthorizedException;
import com.itorly.rph.organization.OrganizationMember;
import com.itorly.rph.organization.OrganizationMemberRepository;
import com.itorly.rph.project.dto.*;
import com.itorly.rph.security.SecurityUtils;
import com.itorly.rph.user.User;
import com.itorly.rph.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BoardService {

    private final ProjectRepository projectRepository;
    private final BoardColumnRepository boardColumnRepository;
    private final TaskRepository taskRepository;
    private final ActivityLogRepository activityLogRepository;
    private final OrganizationMemberRepository memberRepository;
    private final UserRepository userRepository;

    public BoardService(ProjectRepository projectRepository,
                        BoardColumnRepository boardColumnRepository,
                        TaskRepository taskRepository,
                        ActivityLogRepository activityLogRepository,
                        OrganizationMemberRepository memberRepository,
                        UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.boardColumnRepository = boardColumnRepository;
        this.taskRepository = taskRepository;
        this.activityLogRepository = activityLogRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public BoardResponse getBoard(Long projectId) {
        Project project = getAuthorizedProject(projectId);

        List<BoardColumn> columns = boardColumnRepository
                .findByProjectIdOrderByPositionAsc(projectId);

        // Option 1: load tasks per column
        Map<Long, List<Task>> tasksByColumn = new HashMap<>();
        for (BoardColumn col : columns) {
            List<Task> tasks = taskRepository.findByColumnIdOrderByIdAsc(col.getId());
            tasksByColumn.put(col.getId(), tasks);
        }

        List<BoardColumnResponse> columnResponses = columns.stream()
                .map(col -> {
                    List<Task> tasks = tasksByColumn.getOrDefault(col.getId(), List.of());
                    List<TaskResponse> taskResponses = tasks.stream()
                            .map(this::toTaskResponse)
                            .collect(Collectors.toList());
                    return new BoardColumnResponse(
                            col.getId(),
                            col.getName(),
                            col.getPosition(),
                            taskResponses
                    );
                })
                .toList();

        return new BoardResponse(project.getId(), project.getName(), columnResponses);
    }

    @Transactional(readOnly = true)
    public BoardColumnResponse getColumn(Long projectId, Long columnId) {
        Project project = getAuthorizedProject(projectId);

        BoardColumn column = boardColumnRepository.findById(columnId)
                .orElseThrow(() -> new EntityNotFoundException("Column not found"));

        if (!Objects.equals(column.getProject().getId(), project.getId())) {
            throw new BadRequestException("Column does not belong to this project");
        }

        List<TaskResponse> taskResponses = taskRepository.findByColumnIdOrderByIdAsc(columnId)
                .stream()
                .map(this::toTaskResponse)
                .toList();

        return new BoardColumnResponse(
                column.getId(),
                column.getName(),
                column.getPosition(),
                taskResponses
        );
    }

    @Transactional
    public BoardColumnResponse createColumn(Long projectId, CreateColumnRequest request) {
        Project project = getAuthorizedProject(projectId);

        List<BoardColumn> existing = boardColumnRepository
                .findByProjectIdOrderByPositionAsc(projectId);

        int position;
        if (request.getPosition() == null) {
            position = existing.isEmpty()
                    ? 0
                    : existing.get(existing.size() - 1).getPosition() + 1;
        } else {
            position = request.getPosition();
        }

        BoardColumn column = new BoardColumn();
        column.setProject(project);
        column.setName(request.getName());
        column.setPosition(position);

        BoardColumn saved = boardColumnRepository.save(column);

        return new BoardColumnResponse(
                saved.getId(),
                saved.getName(),
                saved.getPosition(),
                List.of()
        );
    }

    @Transactional
    public BoardColumnResponse updateColumn(Long projectId, Long columnId, UpdateColumnRequest request) {
        Project project = getAuthorizedProject(projectId);

        BoardColumn column = boardColumnRepository.findById(columnId)
                .orElseThrow(() -> new EntityNotFoundException("Column not found"));

        if (!Objects.equals(column.getProject().getId(), project.getId())) {
            throw new BadRequestException("Column does not belong to this project");
        }

        if (request.getName() != null) {
            column.setName(request.getName());
        }

        if (request.getPosition() != null) {
            column.setPosition(request.getPosition());
        }

        BoardColumn saved = boardColumnRepository.save(column);

        List<TaskResponse> taskResponses = taskRepository.findByColumnIdOrderByIdAsc(saved.getId())
                .stream()
                .map(this::toTaskResponse)
                .toList();

        return new BoardColumnResponse(
                saved.getId(),
                saved.getName(),
                saved.getPosition(),
                taskResponses
        );
    }

    @Transactional
    public void deleteColumn(Long projectId, Long columnId) {
        Project project = getAuthorizedProject(projectId);

        BoardColumn column = boardColumnRepository.findById(columnId)
                .orElseThrow(() -> new EntityNotFoundException("Column not found"));

        if (!Objects.equals(column.getProject().getId(), project.getId())) {
            throw new BadRequestException("Column does not belong to this project");
        }

        boardColumnRepository.delete(column);
    }

    @Transactional
    public TaskResponse createTask(Long projectId, CreateTaskRequest request) {
        User currentUser = getCurrentUserOrThrow();
        Project project = getAuthorizedProject(projectId, currentUser);

        BoardColumn column = boardColumnRepository.findById(request.getColumnId())
                .orElseThrow(() -> new EntityNotFoundException("Column not found"));

        if (!Objects.equals(column.getProject().getId(), project.getId())) {
            throw new BadRequestException("Column does not belong to this project");
        }

        Task task = new Task();
        task.setProject(project);
        task.setColumn(column);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setTags(request.getTags());
        task.setDueDate(request.getDueDate());

        // Set status based on column name (simple mapping)
        task.setStatus(mapColumnNameToStatus(column.getName()));

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new EntityNotFoundException("Assignee not found"));
            task.setAssignee(assignee);
        }

        Task saved = taskRepository.save(task);

        // Persist an audit trail entry for the newly created task including the actor and column context
        logActivity(project, saved, ActivityActionType.TASK_CREATED, null, column.getName(), currentUser);

        return toTaskResponse(saved);
    }

    @Transactional
    public TaskResponse moveTask(Long projectId, Long taskId, MoveTaskRequest request) {
        User currentUser = getCurrentUserOrThrow();
        Project project = getAuthorizedProject(projectId, currentUser);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        if (!Objects.equals(task.getProject().getId(), project.getId())) {
            throw new BadRequestException("Task does not belong to this project");
        }

        BoardColumn targetColumn = boardColumnRepository.findById(request.getTargetColumnId())
                .orElseThrow(() -> new EntityNotFoundException("Target column not found"));

        if (!Objects.equals(targetColumn.getProject().getId(), project.getId())) {
            throw new BadRequestException("Target column does not belong to this project");
        }

        String oldColumnName = task.getColumn().getName();

        task.setColumn(targetColumn);
        task.setStatus(mapColumnNameToStatus(targetColumn.getName()));

        Task saved = taskRepository.save(task);

        // Capture movement details (old/new column names) alongside the acting user
        logActivity(project, saved, ActivityActionType.TASK_MOVED, oldColumnName, targetColumn.getName(), currentUser);

        return toTaskResponse(saved);
    }

    @Transactional
    public TaskResponse updateTask(Long projectId, Long taskId, UpdateTaskRequest request) {
        User currentUser = getCurrentUserOrThrow();
        Project project = getAuthorizedProject(projectId, currentUser);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        if (!Objects.equals(task.getProject().getId(), project.getId())) {
            throw new BadRequestException("Task does not belong to this project");
        }

        String oldSnapshot = summarizeTask(task);

        if (request.getTitle() != null) {
            if (!StringUtils.hasText(request.getTitle())) {
                throw new BadRequestException("Title cannot be blank");
            }
            task.setTitle(request.getTitle());
        }

        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }

        if (request.getDueDate() != null) {
            task.setDueDate(request.getDueDate());
        } else if (Boolean.TRUE.equals(request.getClearDueDate())) {
            task.setDueDate(null);
        }

        if (request.getTags() != null) {
            task.setTags(request.getTags());
        } else if (Boolean.TRUE.equals(request.getClearTags())) {
            task.setTags(null);
        }

        if (request.getAssigneeId() != null) {
            User assignee = userRepository.findById(request.getAssigneeId())
                    .orElseThrow(() -> new EntityNotFoundException("Assignee not found"));
            task.setAssignee(assignee);
        } else if (Boolean.TRUE.equals(request.getClearAssignee())) {
            task.setAssignee(null);
        }

        Task saved = taskRepository.save(task);

        logActivity(project, saved, ActivityActionType.TASK_UPDATED, oldSnapshot, summarizeTask(saved), currentUser);

        return toTaskResponse(saved);
    }

    @Transactional
    public void deleteTask(Long projectId, Long taskId) {
        User currentUser = getCurrentUserOrThrow();
        Project project = getAuthorizedProject(projectId, currentUser);

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        if (!Objects.equals(task.getProject().getId(), project.getId())) {
            throw new BadRequestException("Task does not belong to this project");
        }

        String snapshot = summarizeTask(task);

        logActivity(project, task, ActivityActionType.TASK_DELETED, snapshot, null, currentUser);

        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getActivity(Long projectId) {
        Project project = getAuthorizedProject(projectId);

        List<ActivityLog> logs = activityLogRepository.findByProjectIdOrderByCreatedAtDesc(project.getId());

        return logs.stream()
                .map(this::toActivityResponse)
                .toList();
    }

    private Project getAuthorizedProject(Long projectId) {
        User currentUser = getCurrentUserOrThrow();
        return getAuthorizedProject(projectId, currentUser);
    }

    private Project getAuthorizedProject(Long projectId, User currentUser) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        Long orgId = project.getOrganization().getId();
        memberRepository
                .findByOrganizationIdAndUserId(orgId, currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("User is not a member of this organization"));

        // For now, any member can view and modify board. Later we can restrict this.
        return project;
    }

    private User getCurrentUserOrThrow() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            throw new UnauthorizedException("No authenticated user");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private TaskResponse toTaskResponse(Task task) {
        Long assigneeId = null;
        String assigneeName = null;
        if (task.getAssignee() != null) {
            assigneeId = task.getAssignee().getId();
            assigneeName = task.getAssignee().getDisplayName();
        }

        return new TaskResponse(
                task.getId(),
                task.getColumn().getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                assigneeId,
                assigneeName,
                task.getDueDate(),
                task.getTags()
        );
    }

    private ActivityLogResponse toActivityResponse(ActivityLog log) {
        Task task = log.getTask();
        User actor = log.getActor();

        Long taskId = task != null ? task.getId() : null;
        String taskTitle = task != null ? task.getTitle() : null;
        Long actorId = actor != null ? actor.getId() : null;
        String actorDisplayName = actor != null ? actor.getDisplayName() : null;

        // Response mirrors persisted log values so the frontend can render a concise activity timeline
        return new ActivityLogResponse(
                log.getId(),
                log.getProject().getId(),
                taskId,
                taskTitle,
                log.getActionType(),
                log.getOldValue(),
                log.getNewValue(),
                actorId,
                actorDisplayName,
                log.getCreatedAt()
        );
    }

    private void logActivity(Project project, Task task, ActivityActionType actionType,
                             String oldValue, String newValue, User actor) {
        ActivityLog log = new ActivityLog();
        log.setProject(project);
        log.setTask(task);
        log.setActionType(actionType);
        log.setOldValue(oldValue);
        log.setNewValue(newValue);
        log.setActor(actor);

        activityLogRepository.save(log);
    }

    private TaskStatus mapColumnNameToStatus(String columnName) {
        String normalized = columnName.trim().toLowerCase();
        return switch (normalized) {
            case "todo" -> TaskStatus.TODO;
            case "in progress" -> TaskStatus.IN_PROGRESS;
            case "review" -> TaskStatus.REVIEW;
            case "done" -> TaskStatus.DONE;
            default -> TaskStatus.TODO;
        };
    }

    private String summarizeTask(Task task) {
        Long assigneeId = task.getAssignee() != null ? task.getAssignee().getId() : null;
        return "title='" + task.getTitle() + '\'' +
                ", description='" + Objects.toString(task.getDescription(), "") + '\'' +
                ", assigneeId=" + assigneeId +
                ", dueDate=" + task.getDueDate() +
                ", tags='" + Objects.toString(task.getTags(), "") + '\'' +
                ", status=" + task.getStatus();
    }
}
