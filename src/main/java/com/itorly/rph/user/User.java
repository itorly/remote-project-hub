package com.itorly.rph.user;

import com.itorly.rph.common.BaseEntity;
import com.itorly.rph.organization.OrganizationMember;
import com.itorly.rph.project.Task;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, length = 100)
    private String displayName;

    @Column(nullable = false, length = 50)
    private String timezone = "UTC";

    @OneToMany(mappedBy = "user")
    private Set<OrganizationMember> memberships = new HashSet<>();

    @OneToMany(mappedBy = "assignee")
    private List<Task> assignedTasks;

    // getters/setters
}