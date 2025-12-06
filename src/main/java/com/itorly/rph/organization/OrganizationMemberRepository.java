package com.itorly.rph.organization;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, Long> {

    /**
     * This will help us:
     *  List orgs for a user
     *  Check membership / role
     */
    List<OrganizationMember> findByUserId(Long userId);

    Optional<OrganizationMember> findByOrganizationIdAndUserId(Long organizationId, Long userId);
}

