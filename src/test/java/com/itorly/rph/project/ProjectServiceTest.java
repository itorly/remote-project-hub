package com.itorly.rph.project;

import com.itorly.rph.organization.*;
import com.itorly.rph.security.SecurityUtils;
import com.itorly.rph.user.User;
import com.itorly.rph.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository memberRepository;

    @Mock
    private UserRepository userRepository;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(
                projectRepository,
                organizationRepository,
                memberRepository,
                userRepository
        );
    }

    @Test
    void createProject_whenUserIsOwner_createsProject() {
        Long orgId = 1L;
        String currentUserEmail = "owner@example.com";

        // Static mocking of SecurityUtils
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            // Prepare a User
            User user = new User();
            user.setId(100L);
            user.setEmail(currentUserEmail);
            user.setDisplayName("Owner User");

            when(userRepository.findByEmail(currentUserEmail))
                    .thenReturn(Optional.of(user));

            // Prepare an Organization
            Organization org = new Organization();
            org.setId(orgId);
            org.setName("Org A");
            org.setOwner(user);

            when(organizationRepository.findById(orgId))
                    .thenReturn(Optional.of(org));

            // Membership as OWNER
            OrganizationMember member = new OrganizationMember();
            member.setId(200L);
            member.setOrganization(org);
            member.setUser(user);
            member.setRole(OrganizationRole.OWNER);
            member.setJoinedAt(Instant.now());

            when(memberRepository.findByOrganizationIdAndUserId(orgId, user.getId()))
                    .thenReturn(Optional.of(member));

            // Incoming request
            com.itorly.rph.project.dto.CreateProjectRequest request =
                    new com.itorly.rph.project.dto.CreateProjectRequest();
            request.setName("First Project");
            request.setDescription("My first project");

            // Configure projectRepository.save(...) to return a persisted Project
            Project savedProject = new Project();
            savedProject.setId(300L);
            savedProject.setName("First Project");
            savedProject.setDescription("My first project");
            savedProject.setStatus(ProjectStatus.ACTIVE);
            savedProject.setOrganization(org);

            when(projectRepository.save(any(Project.class)))
                    .thenReturn(savedProject);

            // Act
            var response = projectService.createProject(orgId, request);

            // Assert
            assertNotNull(response);
            assertEquals(300L, response.getId());
            assertEquals("First Project", response.getName());
            assertEquals("My first project", response.getDescription());
            assertEquals(ProjectStatus.ACTIVE, response.getStatus());
            assertEquals(orgId, response.getOrganizationId());

            // Optionally verify save was called once
            verify(projectRepository, times(1)).save(any(Project.class));
        }
    }

    @Test
    void createProject_whenUserIsNotMember_throwsIllegalStateException() {
        Long orgId = 1L;
        String currentUserEmail = "stranger@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            // User exists
            User user = new User();
            user.setId(100L);
            user.setEmail(currentUserEmail);
            user.setDisplayName("Stranger");

            when(userRepository.findByEmail(currentUserEmail))
                    .thenReturn(Optional.of(user));

            // Org exists
            Organization org = new Organization();
            org.setId(orgId);
            org.setName("Org A");

            when(organizationRepository.findById(orgId))
                    .thenReturn(Optional.of(org));

            // No membership
            when(memberRepository.findByOrganizationIdAndUserId(orgId, user.getId()))
                    .thenReturn(Optional.empty());

            com.itorly.rph.project.dto.CreateProjectRequest request =
                    new com.itorly.rph.project.dto.CreateProjectRequest();
            request.setName("First Project");
            request.setDescription("My first project");

            // Act + Assert: expect IllegalStateException
            IllegalStateException ex = assertThrows(
                    IllegalStateException.class,
                    () -> projectService.createProject(orgId, request)
            );

            assertTrue(ex.getMessage().contains("not a member"));
            verify(projectRepository, never()).save(any(Project.class));
        }
    }

    @Test
    void getProjectsForOrganization_returnsProjectsForMember() {
        Long orgId = 1L;
        String currentUserEmail = "member@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            // User
            User user = new User();
            user.setId(100L);
            user.setEmail(currentUserEmail);

            when(userRepository.findByEmail(currentUserEmail))
                    .thenReturn(Optional.of(user));

            // Org
            Organization org = new Organization();
            org.setId(orgId);
            org.setName("Org A");

            when(organizationRepository.findById(orgId))
                    .thenReturn(Optional.of(org));

            // Membership (any role is fine: MEMBER here)
            OrganizationMember member = new OrganizationMember();
            member.setId(200L);
            member.setOrganization(org);
            member.setUser(user);
            member.setRole(OrganizationRole.MEMBER);

            when(memberRepository.findByOrganizationIdAndUserId(orgId, user.getId()))
                    .thenReturn(Optional.of(member));

            // Projects
            Project p1 = new Project();
            p1.setId(300L);
            p1.setName("P1");
            p1.setDescription("First");
            p1.setStatus(ProjectStatus.ACTIVE);
            p1.setOrganization(org);

            Project p2 = new Project();
            p2.setId(301L);
            p2.setName("P2");
            p2.setDescription("Second");
            p2.setStatus(ProjectStatus.ARCHIVED);
            p2.setOrganization(org);

            when(projectRepository.findByOrganizationId(orgId))
                    .thenReturn(List.of(p1, p2));

            // Act
            var result = projectService.getProjectsForOrganization(orgId);

            // Assert
            assertEquals(2, result.size());
            assertEquals(300L, result.get(0).getId());
            assertEquals("P1", result.get(0).getName());
            assertEquals(ProjectStatus.ACTIVE, result.get(0).getStatus());
            assertEquals(orgId, result.get(0).getOrganizationId());

            assertEquals(301L, result.get(1).getId());
            assertEquals("P2", result.get(1).getName());
            assertEquals(ProjectStatus.ARCHIVED, result.get(1).getStatus());
            assertEquals(orgId, result.get(1).getOrganizationId());
        }
    }



}
