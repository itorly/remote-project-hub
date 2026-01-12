package com.itorly.rph.project;

import com.itorly.rph.common.BaseEntity;
import com.itorly.rph.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(
        name = "activity_logs",
        indexes = {
                @Index(name = "idx_board_activity_project_created", columnList = "project_id, created_at"),
                @Index(name = "idx_board_activity_task_created", columnList = "task_id, created_at")
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
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

    @Column(name = "from_column_id")
    private Long fromColumnId;

    @Column(name = "to_column_id")
    private Long toColumnId;

    @Column(name = "from_position")
    private Integer fromPosition;

    @Column(name = "to_position")
    private Integer toPosition;

    @Column(length = 4000)
    private String oldValue;

    @Column(length = 4000)
    private String newValue;

    @Column(name = "metadata_json", length = 4000)
    private String metadataJson;

}
