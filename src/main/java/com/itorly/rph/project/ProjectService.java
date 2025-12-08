package com.itorly.rph.project;

import com.itorly.rph.organization.Organization;
import com.itorly.rph.organization.OrganizationMember;
import com.itorly.rph.organization.OrganizationMemberRepository;
import com.itorly.rph.organization.OrganizationRole;
import com.itorly.rph.organization.OrganizationRepository;
import com.itorly.rph.project.dto.CreateProjectRequest;
import com.itorly.rph.project.dto.ProjectResponse;
import com.itorly.rph.security.SecurityUtils;
import com.itorly.rph.user.User;
import com.itorly.rph.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final UserRepository userRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository memberRepository,
            UserRepository userRepository
    ) {
        this.projectRepository = projectRepository;
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ProjectResponse createProject(Long organizationId, CreateProjectRequest request) {
        User currentUser = getCurrentUserOrThrow();

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        OrganizationMember membership = memberRepository
                .findByOrganizationIdAndUserId(org.getId(), currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("User is not a member of this organization"));

        // Only OWNER or ADMIN can create projects
        if (membership.getRole() != OrganizationRole.OWNER &&
                membership.getRole() != OrganizationRole.ADMIN) {
            throw new IllegalStateException("User is not allowed to create projects for this organization");
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOrganization(org);

        Project saved = projectRepository.save(project);

        return new ProjectResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getStatus(),
                org.getId()
        );
    }

    @Transactional(readOnly = true)
    public List<ProjectResponse> getProjectsForOrganization(Long organizationId) {
        User currentUser = getCurrentUserOrThrow();

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        // Ensure current user is at least a member
        memberRepository.findByOrganizationIdAndUserId(org.getId(), currentUser.getId())
                .orElseThrow(() -> new IllegalStateException("User is not a member of this organization"));

        List<Project> projects = projectRepository.findByOrganizationId(org.getId());

        return projects.stream()
                .map(p -> new ProjectResponse(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getStatus(),
                        org.getId()
                ))
                .toList();
    }

    private User getCurrentUserOrThrow() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            throw new IllegalStateException("No authenticated user");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}
