package com.itorly.rph.organization;

import com.itorly.rph.common.exception.ConflictException;
import com.itorly.rph.common.exception.ForbiddenException;
import com.itorly.rph.organization.dto.AddOrganizationMemberRequest;
import com.itorly.rph.organization.dto.OrganizationMemberResponse;
import com.itorly.rph.organization.dto.UpdateOrganizationMemberRoleRequest;
import com.itorly.rph.security.SecurityUtils;
import com.itorly.rph.user.User;
import com.itorly.rph.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationMemberServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository memberRepository;

    @Mock
    private UserRepository userRepository;

    private OrganizationMemberService memberService;

    @BeforeEach
    void setUp() {
        memberService = new OrganizationMemberService(
                organizationRepository,
                memberRepository,
                userRepository
        );
    }

    @Test
    void addMember_whenAdmin_addsMember() {
        String adminEmail = "admin@example.com";
        String memberEmail = "member@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(adminEmail);

            User admin = new User();
            admin.setId(1L);
            admin.setEmail(adminEmail);

            User memberUser = new User();
            memberUser.setId(2L);
            memberUser.setEmail(memberEmail);
            memberUser.setDisplayName("Member User");

            Organization organization = new Organization();
            organization.setId(10L);

            OrganizationMember adminMembership = new OrganizationMember();
            adminMembership.setId(100L);
            adminMembership.setOrganization(organization);
            adminMembership.setUser(admin);
            adminMembership.setRole(OrganizationRole.ADMIN);

            when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
            when(userRepository.findByEmail(memberEmail)).thenReturn(Optional.of(memberUser));
            when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
            when(memberRepository.findByOrganizationIdAndUserId(10L, 1L))
                    .thenReturn(Optional.of(adminMembership));
            when(memberRepository.findByOrganizationIdAndUserId(10L, 2L))
                    .thenReturn(Optional.empty());
            when(memberRepository.save(any(OrganizationMember.class)))
                    .thenAnswer(invocation -> {
                        OrganizationMember saved = invocation.getArgument(0);
                        saved.setId(200L);
                        saved.setJoinedAt(Instant.now());
                        return saved;
                    });

            AddOrganizationMemberRequest request = new AddOrganizationMemberRequest();
            request.setEmail(memberEmail);
            request.setRole(OrganizationRole.MEMBER);

            OrganizationMemberResponse response = memberService.addMember(10L, request);

            assertNotNull(response);
            assertEquals(200L, response.id());
            assertEquals(2L, response.userId());
            assertEquals(OrganizationRole.MEMBER, response.role());
        }
    }

    @Test
    void addMember_whenNotAdmin_throwsForbidden() {
        String memberEmail = "member@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(memberEmail);

            User memberUser = new User();
            memberUser.setId(2L);
            memberUser.setEmail(memberEmail);

            Organization organization = new Organization();
            organization.setId(10L);

            OrganizationMember membership = new OrganizationMember();
            membership.setOrganization(organization);
            membership.setUser(memberUser);
            membership.setRole(OrganizationRole.MEMBER);

            when(userRepository.findByEmail(memberEmail)).thenReturn(Optional.of(memberUser));
            when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
            when(memberRepository.findByOrganizationIdAndUserId(10L, 2L))
                    .thenReturn(Optional.of(membership));

            AddOrganizationMemberRequest request = new AddOrganizationMemberRequest();
            request.setEmail("new@example.com");

            assertThrows(ForbiddenException.class, () -> memberService.addMember(10L, request));
        }
    }

    @Test
    void updateMemberRole_whenDemotingLastAdmin_throwsConflict() {
        String adminEmail = "admin@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(adminEmail);

            User admin = new User();
            admin.setId(1L);
            admin.setEmail(adminEmail);

            Organization organization = new Organization();
            organization.setId(10L);

            OrganizationMember adminMembership = new OrganizationMember();
            adminMembership.setOrganization(organization);
            adminMembership.setUser(admin);
            adminMembership.setRole(OrganizationRole.ADMIN);

            OrganizationMember targetMember = new OrganizationMember();
            targetMember.setId(200L);
            targetMember.setOrganization(organization);
            targetMember.setRole(OrganizationRole.ADMIN);

            when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
            when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
            when(memberRepository.findByOrganizationIdAndUserId(10L, 1L))
                    .thenReturn(Optional.of(adminMembership));
            when(memberRepository.findByIdAndOrganizationId(200L, 10L))
                    .thenReturn(Optional.of(targetMember));
            when(memberRepository.countByOrganizationIdAndRoleIn(eq(10L), eq(EnumSet.of(OrganizationRole.OWNER, OrganizationRole.ADMIN))))
                    .thenReturn(1L);

            UpdateOrganizationMemberRoleRequest request = new UpdateOrganizationMemberRoleRequest();
            request.setRole(OrganizationRole.MEMBER);

            assertThrows(ConflictException.class,
                    () -> memberService.updateMemberRole(10L, 200L, request));
        }
    }

    @Test
    void removeMember_whenLastAdmin_throwsConflict() {
        String adminEmail = "admin@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail).thenReturn(adminEmail);

            User admin = new User();
            admin.setId(1L);
            admin.setEmail(adminEmail);

            Organization organization = new Organization();
            organization.setId(10L);

            OrganizationMember adminMembership = new OrganizationMember();
            adminMembership.setOrganization(organization);
            adminMembership.setUser(admin);
            adminMembership.setRole(OrganizationRole.ADMIN);

            OrganizationMember targetMember = new OrganizationMember();
            targetMember.setId(200L);
            targetMember.setOrganization(organization);
            targetMember.setRole(OrganizationRole.ADMIN);

            when(userRepository.findByEmail(adminEmail)).thenReturn(Optional.of(admin));
            when(organizationRepository.findById(10L)).thenReturn(Optional.of(organization));
            when(memberRepository.findByOrganizationIdAndUserId(10L, 1L))
                    .thenReturn(Optional.of(adminMembership));
            when(memberRepository.findByIdAndOrganizationId(200L, 10L))
                    .thenReturn(Optional.of(targetMember));
            when(memberRepository.countByOrganizationIdAndRoleIn(eq(10L), eq(EnumSet.of(OrganizationRole.OWNER, OrganizationRole.ADMIN))))
                    .thenReturn(1L);

            assertThrows(ConflictException.class, () -> memberService.removeMember(10L, 200L));
        }
    }
}
