package com.itorly.rph.organization;

import com.itorly.rph.common.exception.ConflictException;
import com.itorly.rph.common.exception.ForbiddenException;
import com.itorly.rph.common.exception.UnauthorizedException;
import com.itorly.rph.organization.dto.AddOrganizationMemberRequest;
import com.itorly.rph.organization.dto.OrganizationMemberResponse;
import com.itorly.rph.organization.dto.UpdateOrganizationMemberRoleRequest;
import com.itorly.rph.security.SecurityUtils;
import com.itorly.rph.user.User;
import com.itorly.rph.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@Service
public class OrganizationMemberService {

    private static final Set<OrganizationRole> ADMIN_ROLES =
            EnumSet.of(OrganizationRole.OWNER, OrganizationRole.ADMIN);

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final UserRepository userRepository;

    public OrganizationMemberService(
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository memberRepository,
            UserRepository userRepository
    ) {
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrganizationMemberResponse addMember(Long organizationId, AddOrganizationMemberRequest request) {
        User currentUser = getCurrentUserOrThrow();
        Organization organization = getOrganizationOrThrow(organizationId);
        requireAdmin(organization, currentUser);

        User targetUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        memberRepository.findByOrganizationIdAndUserId(organization.getId(), targetUser.getId())
                .ifPresent(existing -> {
                    throw new ConflictException("User is already a member of this organization");
                });

        OrganizationRole requestedRole = request.getRole() != null
                ? request.getRole()
                : OrganizationRole.MEMBER;

        if (requestedRole == OrganizationRole.OWNER) {
            throw new ForbiddenException("Cannot assign OWNER role via membership management");
        }

        OrganizationMember member = new OrganizationMember();
        member.setOrganization(organization);
        member.setUser(targetUser);
        member.setRole(requestedRole);
        member.setJoinedAt(Instant.now());

        OrganizationMember saved = memberRepository.save(member);
        return toResponse(saved);
    }

    @Transactional
    public OrganizationMemberResponse updateMemberRole(
            Long organizationId,
            Long memberId,
            UpdateOrganizationMemberRoleRequest request
    ) {
        User currentUser = getCurrentUserOrThrow();
        Organization organization = getOrganizationOrThrow(organizationId);
        requireAdmin(organization, currentUser);

        OrganizationMember member = memberRepository
                .findByIdAndOrganizationId(memberId, organization.getId())
                .orElseThrow(() -> new EntityNotFoundException("Organization member not found"));

        OrganizationRole newRole = request.getRole();
        if (newRole == OrganizationRole.OWNER) {
            throw new ForbiddenException("Cannot assign OWNER role via membership management");
        }

        if (ADMIN_ROLES.contains(member.getRole())
                && newRole == OrganizationRole.MEMBER
                && isLastAdmin(organization.getId())) {
            throw new ConflictException("Organization must have at least one admin");
        }

        member.setRole(newRole);
        OrganizationMember saved = memberRepository.save(member);
        return toResponse(saved);
    }

    @Transactional
    public void removeMember(Long organizationId, Long memberId) {
        User currentUser = getCurrentUserOrThrow();
        Organization organization = getOrganizationOrThrow(organizationId);
        requireAdmin(organization, currentUser);

        OrganizationMember member = memberRepository
                .findByIdAndOrganizationId(memberId, organization.getId())
                .orElseThrow(() -> new EntityNotFoundException("Organization member not found"));

        if (ADMIN_ROLES.contains(member.getRole()) && isLastAdmin(organization.getId())) {
            throw new ConflictException("Organization must have at least one admin");
        }

        memberRepository.delete(member);
    }

    private boolean isLastAdmin(Long organizationId) {
        long adminCount = memberRepository.countByOrganizationIdAndRoleIn(organizationId, ADMIN_ROLES);
        return adminCount <= 1;
    }

    private void requireAdmin(Organization organization, User user) {
        OrganizationMember membership = memberRepository
                .findByOrganizationIdAndUserId(organization.getId(), user.getId())
                .orElseThrow(() -> new ForbiddenException("User is not a member of this organization"));

        if (!ADMIN_ROLES.contains(membership.getRole())) {
            throw new ForbiddenException("Only admins can manage organization members");
        }
    }

    private Organization getOrganizationOrThrow(Long organizationId) {
        return organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));
    }

    private User getCurrentUserOrThrow() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            throw new UnauthorizedException("No authenticated user");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    private OrganizationMemberResponse toResponse(OrganizationMember member) {
        return new OrganizationMemberResponse(
                member.getId(),
                member.getUser().getId(),
                member.getUser().getEmail(),
                member.getUser().getDisplayName(),
                member.getRole(),
                member.getJoinedAt()
        );
    }
}
