package com.itorly.rph.organization;

import com.itorly.rph.common.BaseEntity;
import com.itorly.rph.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Entity
@Table(
        name = "organization_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"organization_id", "user_id"})
)
@Data
@EqualsAndHashCode(callSuper = true)
public class OrganizationMember extends BaseEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrganizationRole role = OrganizationRole.MEMBER;

    @Column(nullable = false)
    private Instant joinedAt = Instant.now();

}
