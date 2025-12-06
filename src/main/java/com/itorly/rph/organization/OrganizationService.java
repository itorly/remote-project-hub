package com.itorly.rph.organization;

import com.itorly.rph.organization.dto.CreateOrganizationRequest;
import com.itorly.rph.organization.dto.OrganizationResponse;
import com.itorly.rph.security.SecurityUtils;
import com.itorly.rph.user.User;
import com.itorly.rph.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final OrganizationMemberRepository memberRepository;
    private final UserRepository userRepository;

    public OrganizationService(
            OrganizationRepository organizationRepository,
            OrganizationMemberRepository memberRepository,
            UserRepository userRepository
    ) {
        this.organizationRepository = organizationRepository;
        this.memberRepository = memberRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            throw new IllegalStateException("No authenticated user");
        }

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Organization org = new Organization();
        org.setName(request.getName());
        org.setDescription(request.getDescription());
        org.setOwner(currentUser);

        Organization saved = organizationRepository.save(org);

        // Add membership as OWNER
        OrganizationMember member = new OrganizationMember();
        member.setOrganization(saved);
        member.setUser(currentUser);
        member.setRole(OrganizationRole.OWNER);
        member.setJoinedAt(Instant.now());

        memberRepository.save(member);

        return new OrganizationResponse(
                saved.getId(),
                saved.getName(),
                saved.getDescription(),
                OrganizationRole.OWNER
        );
    }

    @Transactional(readOnly = true)
    public List<OrganizationResponse> getMyOrganizations() {
        String email = SecurityUtils.getCurrentUserEmail();
        if (email == null) {
            throw new IllegalStateException("No authenticated user");
        }

        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        List<OrganizationMember> memberships = memberRepository.findByUserId(currentUser.getId());

        return memberships.stream()
                .map(m -> {
                    Organization org = m.getOrganization();
                    return new OrganizationResponse(
                            org.getId(),
                            org.getName(),
                            org.getDescription(),
                            m.getRole()
                    );
                })
                .toList();
    }
}

