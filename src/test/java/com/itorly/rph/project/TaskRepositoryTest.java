package com.itorly.rph.project;

import com.itorly.rph.organization.Organization;
import com.itorly.rph.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void findByProjectId_returnsPagedSortedTasks() {
        Project project = persistProjectWithColumn("Todo");

        Task alpha = buildTask(project, project.getColumns().get(0), "Alpha", 0);
        Task beta = buildTask(project, project.getColumns().get(0), "Beta", 1);
        Task gamma = buildTask(project, project.getColumns().get(0), "Gamma", 2);

        entityManager.persist(alpha);
        entityManager.persist(beta);
        entityManager.persist(gamma);
        entityManager.flush();

        Page<Task> page = taskRepository.findByProjectId(
                project.getId(),
                PageRequest.of(0, 2, Sort.by("title").ascending())
        );

        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        assertEquals("Alpha", page.getContent().get(0).getTitle());
        assertEquals("Beta", page.getContent().get(1).getTitle());
    }

    @Test
    void findByColumnId_returnsPagedSortedTasks() {
        Project project = persistProjectWithColumn("In Progress");
        BoardColumn column = project.getColumns().get(0);

        Task first = buildTask(project, column, "Task A", 1);
        Task second = buildTask(project, column, "Task B", 0);

        entityManager.persist(first);
        entityManager.persist(second);
        entityManager.flush();

        Page<Task> page = taskRepository.findByColumnId(
                column.getId(),
                PageRequest.of(0, 10, Sort.by("position").ascending())
        );

        assertEquals(2, page.getContent().size());
        assertEquals("Task B", page.getContent().get(0).getTitle());
        assertEquals("Task A", page.getContent().get(1).getTitle());
    }

    private Project persistProjectWithColumn(String columnName) {
        User owner = new User();
        owner.setEmail("owner-" + columnName + "@example.com");
        owner.setPasswordHash("hash");
        owner.setDisplayName("Owner");
        entityManager.persist(owner);

        Organization organization = new Organization();
        organization.setName("Org " + columnName);
        organization.setOwner(owner);
        entityManager.persist(organization);

        Project project = new Project();
        project.setName("Project " + columnName);
        project.setDescription("Desc");
        project.setOrganization(organization);
        entityManager.persist(project);

        BoardColumn column = new BoardColumn();
        column.setName(columnName);
        column.setPosition(0);
        column.setProject(project);
        entityManager.persist(column);

        project.setColumns(java.util.List.of(column));

        return project;
    }

    private Task buildTask(Project project, BoardColumn column, String title, int position) {
        Task task = new Task();
        task.setProject(project);
        task.setColumn(column);
        task.setTitle(title);
        task.setDescription("Desc");
        task.setStatus(TaskStatus.TODO);
        task.setPosition(position);
        return task;
    }
}
