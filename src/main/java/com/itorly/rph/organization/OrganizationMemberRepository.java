package com.itorly.rph.organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {

    /**
     * This will help us:
     *  List orgs for a user
     *  Check membership / role
     */
    List<OrganizationMember> findByUserId(Long userId);

    Optional<OrganizationMember> findByOrganizationIdAndUserId(Long organizationId, Long userId);

    Optional<OrganizationMember> findByIdAndOrganizationId(Long id, Long organizationId);

    long countByOrganizationIdAndRoleIn(Long organizationId, Set<OrganizationRole> roles);
}
