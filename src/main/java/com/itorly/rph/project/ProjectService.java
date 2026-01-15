package com.itorly.rph.project;

import com.itorly.rph.common.exception.ForbiddenException;
import com.itorly.rph.common.exception.UnauthorizedException;
import com.itorly.rph.organization.*;
import com.itorly.rph.common.dto.PageResponse;
import com.itorly.rph.project.dto.CreateProjectRequest;
import com.itorly.rph.project.dto.ProjectResponse;
import com.itorly.rph.project.dto.UpdateProjectRequest;
import com.itorly.rph.security.SecurityUtils;
import com.itorly.rph.user.User;
import com.itorly.rph.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final UserRepository userRepository;
    private final BoardColumnRepository boardColumnRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository memberRepository,
            UserRepository userRepository,
            BoardColumnRepository boardColumnRepository
    ) {
        this.projectRepository = projectRepository;
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
        this.boardColumnRepository = boardColumnRepository;
    }

    @Transactional
    public ProjectResponse createProject(Long organizationId, CreateProjectRequest request) {
        User currentUser = getCurrentUserOrThrow();

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        OrganizationMember membership = memberRepository
                .findByOrganizationIdAndUserId(org.getId(), currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("User is not a member of this organization"));

        if (membership.getRole() != OrganizationRole.OWNER &&
                membership.getRole() != OrganizationRole.ADMIN) {
            throw new ForbiddenException("User is not allowed to create projects for this organization");
        }

        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(ProjectStatus.ACTIVE);
        project.setOrganization(org);

        Project saved = projectRepository.save(project);

        createDefaultColumns(saved);

        return new ProjectResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getStatus(),
                org.getId()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<ProjectResponse> getProjectsForOrganization(Long organizationId, Pageable pageable) {
        User currentUser = getCurrentUserOrThrow();

        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        memberRepository.findByOrganizationIdAndUserId(org.getId(), currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("User is not a member of this organization"));

        Page<Project> projects = projectRepository.findByOrganizationId(org.getId(), pageable);
        List<ProjectResponse> items = projects.stream()
                .map(p -> new ProjectResponse(
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getStatus(),
                        org.getId()
                ))
                .toList();
        return new PageResponse<>(
                items,
                projects.getNumber(),
                projects.getSize(),
                projects.getTotalElements(),
                projects.getTotalPages()
        );
    }

    @Transactional
    public ProjectResponse updateProject(Long organizationId, Long projectId, UpdateProjectRequest request) {
        User currentUser = getCurrentUserOrThrow();
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        OrganizationMember membership = memberRepository
                .findByOrganizationIdAndUserId(org.getId(), currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("User is not a member of this organization"));

        if (membership.getRole() != OrganizationRole.OWNER &&
                membership.getRole() != OrganizationRole.ADMIN) {
            throw new ForbiddenException("User is not allowed to update projects for this organization");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        if (!project.getOrganization().getId().equals(org.getId())) {
            throw new ForbiddenException("Project does not belong to this organization");
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStatus(request.getStatus());

        Project saved = projectRepository.save(project);

        return new ProjectResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                saved.getStatus(),
                org.getId()
        );
    }

    @Transactional
    public void deleteProject(Long organizationId, Long projectId) {
        User currentUser = getCurrentUserOrThrow();
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new EntityNotFoundException("Organization not found"));

        OrganizationMember membership = memberRepository
                .findByOrganizationIdAndUserId(org.getId(), currentUser.getId())
                .orElseThrow(() -> new ForbiddenException("User is not a member of this organization"));

        if (membership.getRole() != OrganizationRole.OWNER &&
                membership.getRole() != OrganizationRole.ADMIN) {
            throw new ForbiddenException("User is not allowed to delete projects for this organization");
        }

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new EntityNotFoundException("Project not found"));

        if (!project.getOrganization().getId().equals(org.getId())) {
            throw new ForbiddenException("Project does not belong to this organization");
        }

        projectRepository.delete(project);
    }

    private User getCurrentUserOrThrow() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            throw new UnauthorizedException("No authenticated user");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    /**
     * Every new project will automatically have your 4 Kanban columns.
     */
    private void createDefaultColumns(Project project) {
        BoardColumn todo = new BoardColumn();
        todo.setProject(project);
        todo.setName("Todo");
        todo.setPosition(0);

        BoardColumn inProgress = new BoardColumn();
        inProgress.setProject(project);
        inProgress.setName("In Progress");
        inProgress.setPosition(1);

        BoardColumn review = new BoardColumn();
        review.setProject(project);
        review.setName("Review");
        review.setPosition(2);

        BoardColumn done = new BoardColumn();
        done.setProject(project);
        done.setName("Done");
        done.setPosition(3);

        boardColumnRepository.save(todo);
        boardColumnRepository.save(inProgress);
        boardColumnRepository.save(review);
        boardColumnRepository.save(done);
    }
}
