package com.itorly.rph.project;

import com.itorly.rph.organization.Organization;
import com.itorly.rph.organization.OrganizationMember;
import com.itorly.rph.organization.OrganizationMemberRepository;
import com.itorly.rph.project.dto.ActivityLogResponse;
import com.itorly.rph.project.dto.BoardColumnResponse;
import com.itorly.rph.project.dto.CreateTaskRequest;
import com.itorly.rph.project.dto.MoveTaskRequest;
import com.itorly.rph.project.dto.TaskResponse;
import com.itorly.rph.project.dto.UpdateColumnRequest;
import com.itorly.rph.project.dto.UpdateTaskRequest;
import com.itorly.rph.security.SecurityUtils;
import com.itorly.rph.user.User;
import com.itorly.rph.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private BoardColumnRepository boardColumnRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private OrganizationMemberRepository memberRepository;

    @Mock
    private UserRepository userRepository;

    private BoardService boardService;

    @BeforeEach
    void setUp() {
        boardService = new BoardService(
                projectRepository,
                boardColumnRepository,
                taskRepository,
                activityLogRepository,
                memberRepository,
                userRepository
        );
    }

    @Test
    void getColumn_returnsTasksForAuthorizedProject() {
        Long projectId = 1L;
        Long columnId = 10L;
        String currentUserEmail = "user@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            User user = new User();
            user.setId(100L);
            user.setEmail(currentUserEmail);
            user.setDisplayName("User");

            when(userRepository.findByEmail(currentUserEmail))
                    .thenReturn(Optional.of(user));

            Organization organization = new Organization();
            organization.setId(200L);

            Project project = new Project();
            project.setId(projectId);
            project.setOrganization(organization);

            when(projectRepository.findById(projectId))
                    .thenReturn(Optional.of(project));

            OrganizationMember member = new OrganizationMember();
            member.setId(300L);
            member.setUser(user);
            member.setOrganization(organization);

            when(memberRepository.findByOrganizationIdAndUserId(organization.getId(), user.getId()))
                    .thenReturn(Optional.of(member));

            BoardColumn column = new BoardColumn();
            column.setId(columnId);
            column.setName("Todo");
            column.setPosition(0);
            column.setProject(project);

            when(boardColumnRepository.findById(columnId))
                    .thenReturn(Optional.of(column));

            Task task = new Task();
            task.setId(400L);
            task.setTitle("Investigate bug");
            task.setProject(project);
            task.setColumn(column);
            task.setStatus(TaskStatus.TODO);

            when(taskRepository.findByColumnIdOrderByIdAsc(columnId))
                    .thenReturn(List.of(task));

            BoardColumnResponse response = boardService.getColumn(projectId, columnId);

            assertEquals(columnId, response.getId());
            assertEquals(1, response.getTasks().size());
            assertEquals(task.getId(), response.getTasks().get(0).getId());
        }
    }

    @Test
    void createTask_logsActivityWithActorAndColumnName() {
        Long projectId = 1L;
        Long columnId = 10L;
        String currentUserEmail = "user@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            User user = new User();
            user.setId(100L);
            user.setEmail(currentUserEmail);
            user.setDisplayName("User");

            when(userRepository.findByEmail(currentUserEmail))
                    .thenReturn(Optional.of(user));

            Organization organization = new Organization();
            organization.setId(200L);

            Project project = new Project();
            project.setId(projectId);
            project.setOrganization(organization);

            when(projectRepository.findById(projectId))
                    .thenReturn(Optional.of(project));

            OrganizationMember member = new OrganizationMember();
            member.setId(300L);
            member.setUser(user);
            member.setOrganization(organization);

            when(memberRepository.findByOrganizationIdAndUserId(organization.getId(), user.getId()))
                    .thenReturn(Optional.of(member));

            BoardColumn column = new BoardColumn();
            column.setId(columnId);
            column.setName("Todo");
            column.setProject(project);

            when(boardColumnRepository.findById(columnId))
                    .thenReturn(Optional.of(column));

            when(taskRepository.save(any(Task.class)))
                    .thenAnswer(invocation -> {
                        Task toSave = invocation.getArgument(0);
                        toSave.setId(400L);
                        return toSave;
                    });

            ArgumentCaptor<ActivityLog> logCaptor = ArgumentCaptor.forClass(ActivityLog.class);
            when(activityLogRepository.save(logCaptor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            CreateTaskRequest request = new CreateTaskRequest();
            request.setColumnId(columnId);
            request.setTitle("New Task");
            request.setDescription("Description");

            TaskResponse response = boardService.createTask(projectId, request);

            assertNotNull(response);
            assertEquals(400L, response.getId());
            assertEquals(columnId, response.getColumnId());

            // Logged activity should capture action metadata and the acting user
            ActivityLog captured = logCaptor.getValue();
            assertEquals(ActivityActionType.TASK_CREATED, captured.getActionType());
            assertEquals(project, captured.getProject());
            assertNotNull(captured.getTask());
            assertEquals(400L, captured.getTask().getId());
            assertNull(captured.getOldValue());
            assertEquals(column.getName(), captured.getNewValue());
            assertEquals(user, captured.getActor());
        }
    }

    @Test
    void moveTask_logsMovementAndUpdatesStatus() {
        Long projectId = 1L;
        Long taskId = 2L;
        Long targetColumnId = 3L;
        String currentUserEmail = "user@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            User user = new User();
            user.setId(100L);
            user.setEmail(currentUserEmail);
            user.setDisplayName("Mover");

            when(userRepository.findByEmail(currentUserEmail))
                    .thenReturn(Optional.of(user));

            Organization organization = new Organization();
            organization.setId(200L);

            Project project = new Project();
            project.setId(projectId);
            project.setOrganization(organization);

            when(projectRepository.findById(projectId))
                    .thenReturn(Optional.of(project));

            OrganizationMember member = new OrganizationMember();
            member.setId(300L);
            member.setUser(user);
            member.setOrganization(organization);

            when(memberRepository.findByOrganizationIdAndUserId(organization.getId(), user.getId()))
                    .thenReturn(Optional.of(member));

            BoardColumn currentColumn = new BoardColumn();
            currentColumn.setId(10L);
            currentColumn.setName("Todo");
            currentColumn.setProject(project);

            BoardColumn targetColumn = new BoardColumn();
            targetColumn.setId(targetColumnId);
            targetColumn.setName("Done");
            targetColumn.setProject(project);

            Task task = new Task();
            task.setId(taskId);
            task.setProject(project);
            task.setColumn(currentColumn);
            task.setTitle("Task to move");
            task.setStatus(TaskStatus.TODO);

            when(taskRepository.findById(taskId))
                    .thenReturn(Optional.of(task));

            when(boardColumnRepository.findById(targetColumnId))
                    .thenReturn(Optional.of(targetColumn));

            when(taskRepository.save(any(Task.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<ActivityLog> logCaptor = ArgumentCaptor.forClass(ActivityLog.class);
            when(activityLogRepository.save(logCaptor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            MoveTaskRequest request = new MoveTaskRequest();
            request.setTargetColumnId(targetColumnId);

            TaskResponse response = boardService.moveTask(projectId, taskId, request);

            assertEquals(targetColumnId, response.getColumnId());
            assertEquals(TaskStatus.DONE, task.getStatus());

            // Movement activity should log both column names along with the actor
            ActivityLog captured = logCaptor.getValue();
            assertEquals(ActivityActionType.TASK_MOVED, captured.getActionType());
            assertEquals(currentColumn.getName(), captured.getOldValue());
            assertEquals(targetColumn.getName(), captured.getNewValue());
            assertEquals(user, captured.getActor());
            assertEquals(task, captured.getTask());
        }
    }

    @Test
    void updateTask_updatesFieldsAndLogsSnapshot() {
        Long projectId = 1L;
        Long taskId = 2L;
        Long newAssigneeId = 999L;
        String currentUserEmail = "editor@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            User currentUser = new User();
            currentUser.setId(100L);
            currentUser.setEmail(currentUserEmail);
            currentUser.setDisplayName("Editor");

            when(userRepository.findByEmail(currentUserEmail))
                    .thenReturn(Optional.of(currentUser));

            User newAssignee = new User();
            newAssignee.setId(newAssigneeId);
            newAssignee.setDisplayName("New Assignee");

            when(userRepository.findById(newAssigneeId))
                    .thenReturn(Optional.of(newAssignee));

            Organization organization = new Organization();
            organization.setId(200L);

            Project project = new Project();
            project.setId(projectId);
            project.setOrganization(organization);

            when(projectRepository.findById(projectId))
                    .thenReturn(Optional.of(project));

            OrganizationMember member = new OrganizationMember();
            member.setId(300L);
            member.setUser(currentUser);
            member.setOrganization(organization);

            when(memberRepository.findByOrganizationIdAndUserId(organization.getId(), currentUser.getId()))
                    .thenReturn(Optional.of(member));

            BoardColumn column = new BoardColumn();
            column.setId(10L);
            column.setProject(project);
            column.setName("Todo");

            User oldAssignee = new User();
            oldAssignee.setId(555L);
            oldAssignee.setDisplayName("Old Assignee");

            Instant oldDueDate = Instant.parse("2024-07-01T00:00:00Z");
            Task task = new Task();
            task.setId(taskId);
            task.setProject(project);
            task.setColumn(column);
            task.setTitle("Legacy task");
            task.setDescription("Old details");
            task.setAssignee(oldAssignee);
            task.setStatus(TaskStatus.TODO);
            task.setDueDate(oldDueDate);
            task.setTags("old,tags");

            when(taskRepository.findById(taskId))
                    .thenReturn(Optional.of(task));

            when(taskRepository.save(any(Task.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<ActivityLog> logCaptor = ArgumentCaptor.forClass(ActivityLog.class);
            when(activityLogRepository.save(logCaptor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setTitle("Updated task");
            request.setDescription("Revised details");
            request.setAssigneeId(newAssigneeId);
            request.setDueDate(Instant.parse("2024-08-15T00:00:00Z"));
            request.setTags("new,tags");

            TaskResponse response = boardService.updateTask(projectId, taskId, request);

            assertEquals("Updated task", response.getTitle());
            assertEquals(newAssigneeId, response.getAssigneeId());
            assertEquals("new,tags", response.getTags());

            assertEquals("Updated task", task.getTitle());
            assertEquals("Revised details", task.getDescription());
            assertEquals(newAssignee, task.getAssignee());
            assertEquals(Instant.parse("2024-08-15T00:00:00Z"), task.getDueDate());
            assertEquals("new,tags", task.getTags());

            ActivityLog captured = logCaptor.getValue();
            assertEquals(ActivityActionType.TASK_UPDATED, captured.getActionType());
            assertEquals(currentUser, captured.getActor());
            assertEquals(task, captured.getTask());
            assertEquals("title='Legacy task', description='Old details', assigneeId=555, dueDate=" + oldDueDate + ", tags='old,tags', status=TODO", captured.getOldValue());
            assertEquals("title='Updated task', description='Revised details', assigneeId=999, dueDate=2024-08-15T00:00:00Z, tags='new,tags', status=TODO", captured.getNewValue());
        }
    }

    @Test
    void updateTask_clearsOptionalFieldsWhenRequested() {
        Long projectId = 1L;
        Long taskId = 2L;
        String currentUserEmail = "editor@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            User currentUser = new User();
            currentUser.setId(100L);
            currentUser.setEmail(currentUserEmail);

            when(userRepository.findByEmail(currentUserEmail))
                    .thenReturn(Optional.of(currentUser));

            Organization organization = new Organization();
            organization.setId(200L);

            Project project = new Project();
            project.setId(projectId);
            project.setOrganization(organization);

            when(projectRepository.findById(projectId))
                    .thenReturn(Optional.of(project));

            OrganizationMember member = new OrganizationMember();
            member.setId(300L);
            member.setUser(currentUser);
            member.setOrganization(organization);

            when(memberRepository.findByOrganizationIdAndUserId(organization.getId(), currentUser.getId()))
                    .thenReturn(Optional.of(member));

            BoardColumn column = new BoardColumn();
            column.setId(10L);
            column.setProject(project);
            column.setName("Todo");

            User assignee = new User();
            assignee.setId(555L);

            Instant dueDate = Instant.parse("2024-08-01T00:00:00Z");
            Task task = new Task();
            task.setId(taskId);
            task.setProject(project);
            task.setColumn(column);
            task.setTitle("Task");
            task.setDescription("Desc");
            task.setAssignee(assignee);
            task.setDueDate(dueDate);
            task.setTags("alpha,beta");
            task.setStatus(TaskStatus.TODO);

            when(taskRepository.findById(taskId))
                    .thenReturn(Optional.of(task));

            when(taskRepository.save(any(Task.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            ArgumentCaptor<ActivityLog> logCaptor = ArgumentCaptor.forClass(ActivityLog.class);
            when(activityLogRepository.save(logCaptor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            UpdateTaskRequest request = new UpdateTaskRequest();
            request.setClearAssignee(true);
            request.setClearDueDate(true);
            request.setClearTags(true);

            TaskResponse response = boardService.updateTask(projectId, taskId, request);

            assertNull(response.getAssigneeId());
            assertNull(response.getDueDate());
            assertNull(response.getTags());

            assertNull(task.getAssignee());
            assertNull(task.getDueDate());
            assertNull(task.getTags());

            ActivityLog captured = logCaptor.getValue();
            assertEquals(ActivityActionType.TASK_UPDATED, captured.getActionType());
            assertEquals("title='Task', description='Desc', assigneeId=555, dueDate=2024-08-01T00:00:00Z, tags='alpha,beta', status=TODO", captured.getOldValue());
            assertEquals("title='Task', description='Desc', assigneeId=null, dueDate=null, tags='', status=TODO", captured.getNewValue());
        }
    }

    @Test
    void deleteTask_logsSnapshotAndDeletes() {
        Long projectId = 1L;
        Long taskId = 2L;
        String currentUserEmail = "deleter@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            User currentUser = new User();
            currentUser.setId(100L);
            currentUser.setEmail(currentUserEmail);

            when(userRepository.findByEmail(currentUserEmail))
                    .thenReturn(Optional.of(currentUser));

            Organization organization = new Organization();
            organization.setId(200L);

            Project project = new Project();
            project.setId(projectId);
            project.setOrganization(organization);

            when(projectRepository.findById(projectId))
                    .thenReturn(Optional.of(project));

            OrganizationMember member = new OrganizationMember();
            member.setId(300L);
            member.setUser(currentUser);
            member.setOrganization(organization);

            when(memberRepository.findByOrganizationIdAndUserId(organization.getId(), currentUser.getId()))
                    .thenReturn(Optional.of(member));

            BoardColumn column = new BoardColumn();
            column.setId(10L);
            column.setProject(project);
            column.setName("Todo");

            Task task = new Task();
            task.setId(taskId);
            task.setProject(project);
            task.setColumn(column);
            task.setTitle("Removable");
            task.setDescription("To be deleted");
            task.setStatus(TaskStatus.TODO);

            when(taskRepository.findById(taskId))
                    .thenReturn(Optional.of(task));

            ArgumentCaptor<ActivityLog> logCaptor = ArgumentCaptor.forClass(ActivityLog.class);
            when(activityLogRepository.save(logCaptor.capture()))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            boardService.deleteTask(projectId, taskId);

            verify(taskRepository).delete(task);

            ActivityLog captured = logCaptor.getValue();
            assertEquals(ActivityActionType.TASK_DELETED, captured.getActionType());
            assertEquals("title='Removable', description='To be deleted', assigneeId=null, dueDate=null, tags='', status=TODO", captured.getOldValue());
            assertNull(captured.getNewValue());
            assertEquals(currentUser, captured.getActor());
            assertEquals(task, captured.getTask());
        }
    }

    @Test
    void updateColumn_updatesNameAndPosition() {
        Long projectId = 1L;
        Long columnId = 2L;
        String currentUserEmail = "user@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            User user = new User();
            user.setId(100L);
            user.setEmail(currentUserEmail);

            when(userRepository.findByEmail(currentUserEmail))
                    .thenReturn(Optional.of(user));

            Organization organization = new Organization();
            organization.setId(200L);

            Project project = new Project();
            project.setId(projectId);
            project.setOrganization(organization);

            when(projectRepository.findById(projectId))
                    .thenReturn(Optional.of(project));

            OrganizationMember member = new OrganizationMember();
            member.setId(300L);
            member.setUser(user);
            member.setOrganization(organization);

            when(memberRepository.findByOrganizationIdAndUserId(organization.getId(), user.getId()))
                    .thenReturn(Optional.of(member));

            BoardColumn column = new BoardColumn();
            column.setId(columnId);
            column.setProject(project);
            column.setName("Todo");
            column.setPosition(0);

            when(boardColumnRepository.findById(columnId)).thenReturn(Optional.of(column));

            when(boardColumnRepository.save(any(BoardColumn.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            when(taskRepository.findByColumnIdOrderByIdAsc(columnId))
                    .thenReturn(List.of());

            UpdateColumnRequest request = new UpdateColumnRequest();
            request.setName("Review");
            request.setPosition(2);

            BoardColumnResponse response = boardService.updateColumn(projectId, columnId, request);

            assertEquals("Review", response.getName());
            assertEquals(2, response.getPosition());

            ArgumentCaptor<BoardColumn> captor = ArgumentCaptor.forClass(BoardColumn.class);
            verify(boardColumnRepository).save(captor.capture());
            assertEquals("Review", captor.getValue().getName());
            assertEquals(2, captor.getValue().getPosition());
        }
    }

    @Test
    void deleteColumn_removesColumnForAuthorizedProject() {
        Long projectId = 1L;
        Long columnId = 2L;
        String currentUserEmail = "user@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            User user = new User();
            user.setId(100L);
            user.setEmail(currentUserEmail);

            when(userRepository.findByEmail(currentUserEmail))
                    .thenReturn(Optional.of(user));

            Organization organization = new Organization();
            organization.setId(200L);

            Project project = new Project();
            project.setId(projectId);
            project.setOrganization(organization);

            when(projectRepository.findById(projectId))
                    .thenReturn(Optional.of(project));

            OrganizationMember member = new OrganizationMember();
            member.setId(300L);
            member.setUser(user);
            member.setOrganization(organization);

            when(memberRepository.findByOrganizationIdAndUserId(organization.getId(), user.getId()))
                    .thenReturn(Optional.of(member));

            BoardColumn column = new BoardColumn();
            column.setId(columnId);
            column.setProject(project);

            when(boardColumnRepository.findById(columnId)).thenReturn(Optional.of(column));

            boardService.deleteColumn(projectId, columnId);

            verify(boardColumnRepository).delete(column);
        }
    }

    @Test
    void getActivity_returnsMappedResponses() {
        Long projectId = 1L;
        String currentUserEmail = "user@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            User user = new User();
            user.setId(100L);
            user.setEmail(currentUserEmail);
            user.setDisplayName("Viewer");

            when(userRepository.findByEmail(currentUserEmail))
                    .thenReturn(Optional.of(user));

            Organization organization = new Organization();
            organization.setId(200L);

            Project project = new Project();
            project.setId(projectId);
            project.setOrganization(organization);

            when(projectRepository.findById(projectId))
                    .thenReturn(Optional.of(project));

            OrganizationMember member = new OrganizationMember();
            member.setId(300L);
            member.setUser(user);
            member.setOrganization(organization);

            when(memberRepository.findByOrganizationIdAndUserId(organization.getId(), user.getId()))
                    .thenReturn(Optional.of(member));

            User actor = new User();
            actor.setId(500L);
            actor.setDisplayName("Actor");

            Task task = new Task();
            task.setId(600L);
            task.setTitle("Task A");

            ActivityLog log = new ActivityLog();
            log.setId(700L);
            log.setProject(project);
            log.setTask(task);
            log.setActor(actor);
            log.setActionType(ActivityActionType.TASK_CREATED);
            log.setOldValue(null);
            log.setNewValue("Todo");
            log.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));

            when(activityLogRepository.findByProjectIdOrderByCreatedAtDesc(projectId))
                    .thenReturn(List.of(log));

            List<ActivityLogResponse> responses = boardService.getActivity(projectId);

            assertEquals(1, responses.size());
            ActivityLogResponse response = responses.get(0);
            assertEquals(log.getId(), response.getId());
            assertEquals(projectId, response.getProjectId());
            assertEquals(task.getId(), response.getTaskId());
            assertEquals(task.getTitle(), response.getTaskTitle());
            assertEquals(ActivityActionType.TASK_CREATED, response.getActionType());
            assertEquals(actor.getId(), response.getActorId());
            assertEquals(actor.getDisplayName(), response.getActorDisplayName());
            assertEquals(log.getCreatedAt(), response.getCreatedAt());
        }
    }
}
