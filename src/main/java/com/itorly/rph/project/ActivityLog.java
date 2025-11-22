package com.itorly.rph.project;

import com.itorly.rph.common.BaseEntity;
import com.itorly.rph.user.User;
import jakarta.persistence.*;

@Entity
@Table(name = "activity_logs")
public class ActivityLog extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne
    @JoinColumn(name = "actor_id")
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ActivityActionType actionType;

    @Column(length = 4000)
    private String oldValue;

    @Column(length = 4000)
    private String newValue;

    // getters/setters
}
