package com.itorly.rph.project;

import com.itorly.rph.common.BaseEntity;
import com.itorly.rph.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Entity
@Table(
        name = "tasks",
        indexes = {
                @Index(name = "idx_tasks_project_created_at", columnList = "project_id, created_at"),
                @Index(name = "idx_tasks_project_updated_at", columnList = "project_id, updated_at"),
                @Index(name = "idx_tasks_project_title", columnList = "project_id, title"),
                @Index(name = "idx_tasks_column_position", columnList = "column_id, position")
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
public class Task extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(optional = false)
    @JoinColumn(name = "column_id")
    private BoardColumn column;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 4000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "assignee_id")
    private User assignee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.TODO;

    @Column
    private Instant dueDate; // store in UTC

    @Column(length = 500)
    private String tags; // comma-separated for MVP

    @Column
    private Integer position;

}
