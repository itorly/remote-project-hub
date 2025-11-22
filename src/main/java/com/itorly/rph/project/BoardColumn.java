package com.itorly.rph.project;

import com.itorly.rph.common.BaseEntity;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "board_columns")
public class BoardColumn extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer position;

    @OneToMany(mappedBy = "column", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;

    // getters/setters
}
