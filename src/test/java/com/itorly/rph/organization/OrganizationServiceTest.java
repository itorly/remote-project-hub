package com.itorly.rph.organization;

import com.itorly.rph.common.exception.UnauthorizedException;
import com.itorly.rph.organization.dto.CreateOrganizationRequest;
import com.itorly.rph.organization.dto.OrganizationResponse;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * What these tests cover:
 *
 * ✅ Happy path for createOrganization (creates org + OWNER membership, returns correct DTO)
 *
 * ✅ Unauthorized (no current user) for both methods
 *
 * ✅ “User not found” branch for both methods
 *
 * ✅ Mapping logic in getMyOrganizations (org fields + member role)
 *
 * They also use the same static-mocking style as your existing ProjectServiceTest.
 */
@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMemberRepository memberRepository;

    @Mock
    private UserRepository userRepository;

    private OrganizationService organizationService;

    @BeforeEach
    void setUp() {
        organizationService = new OrganizationService(
                organizationRepository,
                memberRepository,
                userRepository
        );
    }

    @Test
    void createOrganization_whenUserAuthenticated_createsOrgAndOwnerMembership() {
        String currentUserEmail = "owner@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            User user = new User();
            user.setId(1L);
            user.setEmail(currentUserEmail);
            user.setDisplayName("Owner User");

            when(userRepository.findByEmail(currentUserEmail))
                    .thenReturn(Optional.of(user));

            CreateOrganizationRequest request = new CreateOrganizationRequest();
            request.setName("My Org");
            request.setDescription("Test organization");

            // Configure organizationRepository.save(...) to return a persisted Organization
            Organization savedOrg = new Organization();
            savedOrg.setId(10L);
            savedOrg.setName(request.getName());
            savedOrg.setDescription(request.getDescription());
            savedOrg.setOwner(user);

            when(organizationRepository.save(any(Organization.class)))
                    .thenReturn(savedOrg);

            // We don't care about the return of memberRepository.save() here,
            // only that it is called with correct data.
            when(memberRepository.save(any(OrganizationMember.class)))
                    .thenAnswer(invocation -> {
                        OrganizationMember m = invocation.getArgument(0);
                        if (m.getId() == null) {
                            m.setId(100L);
                        }
                        if (m.getJoinedAt() == null) {
                            m.setJoinedAt(Instant.now());
                        }
                        return m;
                    });

            // Act
            OrganizationResponse response = organizationService.createOrganization(request);

            // Assert
            assertNotNull(response);
            assertEquals(10L, response.getId());
            assertEquals("My Org", response.getName());
            assertEquals("Test organization", response.getDescription());
            assertEquals(OrganizationRole.OWNER, response.getRole());

            verify(userRepository, times(1)).findByEmail(currentUserEmail);
            verify(organizationRepository, times(1)).save(any(Organization.class));
            verify(memberRepository, times(1)).save(any(OrganizationMember.class));
        }
    }

    @Test
    void createOrganization_whenNoAuthenticatedUser_throwsUnauthorizedException() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(null);

            CreateOrganizationRequest request = new CreateOrganizationRequest();
            request.setName("My Org");
            request.setDescription("Test organization");

            // Act + Assert
            UnauthorizedException ex = assertThrows(
                    UnauthorizedException.class,
                    () -> organizationService.createOrganization(request)
            );

            assertTrue(ex.getMessage().contains("No authenticated user"));

            verifyNoInteractions(userRepository, organizationRepository, memberRepository);
        }
    }

    @Test
    void createOrganization_whenUserNotFound_throwsEntityNotFoundException() {
        String currentUserEmail = "missing@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            when(userRepository.findByEmail(currentUserEmail))
                    .thenReturn(Optional.empty());

            CreateOrganizationRequest request = new CreateOrganizationRequest();
            request.setName("My Org");
            request.setDescription("Test organization");

            // Act + Assert
            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> organizationService.createOrganization(request)
            );

            assertTrue(ex.getMessage().contains("User not found"));

            verify(userRepository, times(1)).findByEmail(currentUserEmail);
            verifyNoInteractions(organizationRepository, memberRepository);
        }
    }

    @Test
    void getMyOrganizations_whenNoAuthenticatedUser_throwsUnauthorizedException() {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(null);

            // Act + Assert
            UnauthorizedException ex = assertThrows(
                    UnauthorizedException.class,
                    () -> organizationService.getMyOrganizations()
            );

            assertTrue(ex.getMessage().contains("No authenticated user"));

            verifyNoInteractions(userRepository, organizationRepository, memberRepository);
        }
    }

    @Test
    void getMyOrganizations_whenUserNotFound_throwsEntityNotFoundException() {
        String currentUserEmail = "missing@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            when(userRepository.findByEmail(currentUserEmail))
                    .thenReturn(Optional.empty());

            // Act + Assert
            EntityNotFoundException ex = assertThrows(
                    EntityNotFoundException.class,
                    () -> organizationService.getMyOrganizations()
            );

            assertTrue(ex.getMessage().contains("User not found"));

            verify(userRepository, times(1)).findByEmail(currentUserEmail);
            verifyNoInteractions(memberRepository);
        }
    }

    @Test
    void getMyOrganizations_returnsOrganizationsForCurrentUser() {
        String currentUserEmail = "member@example.com";

        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            // Arrange
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserEmail)
                    .thenReturn(currentUserEmail);

            User user = new User();
            user.setId(5L);
            user.setEmail(currentUserEmail);
            user.setDisplayName("Member User");

            when(userRepository.findByEmail(currentUserEmail))
                    .thenReturn(Optional.of(user));

            Organization org1 = new Organization();
            org1.setId(100L);
            org1.setName("Org 1");
            org1.setDescription("First org");
            org1.setOwner(user);

            Organization org2 = new Organization();
            org2.setId(101L);
            org2.setName("Org 2");
            org2.setDescription("Second org");
            org2.setOwner(user);

            OrganizationMember m1 = new OrganizationMember();
            m1.setId(200L);
            m1.setOrganization(org1);
            m1.setUser(user);
            m1.setRole(OrganizationRole.OWNER);
            m1.setJoinedAt(Instant.now());

            OrganizationMember m2 = new OrganizationMember();
            m2.setId(201L);
            m2.setOrganization(org2);
            m2.setUser(user);
            m2.setRole(OrganizationRole.MEMBER);
            m2.setJoinedAt(Instant.now());

            when(memberRepository.findByUserId(user.getId()))
                    .thenReturn(List.of(m1, m2));

            // Act
            List<OrganizationResponse> result = organizationService.getMyOrganizations();

            // Assert
            assertEquals(2, result.size());

            OrganizationResponse r1 = result.get(0);
            assertEquals(100L, r1.getId());
            assertEquals("Org 1", r1.getName());
            assertEquals("First org", r1.getDescription());
            assertEquals(OrganizationRole.OWNER, r1.getRole());

            OrganizationResponse r2 = result.get(1);
            assertEquals(101L, r2.getId());
            assertEquals("Org 2", r2.getName());
            assertEquals("Second org", r2.getDescription());
            assertEquals(OrganizationRole.MEMBER, r2.getRole());

            verify(memberRepository, times(1)).findByUserId(user.getId());
        }
    }
}
