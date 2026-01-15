package com.itorly.rph.project;

import com.itorly.rph.organization.Organization;
import com.itorly.rph.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ProjectRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    void findByOrganizationId_returnsPagedSortedProjects() {
        User owner = new User();
        owner.setEmail("owner@example.com");
        owner.setPasswordHash("hash");
        owner.setDisplayName("Owner");
        entityManager.persist(owner);

        Organization organization = new Organization();
        organization.setName("Acme");
        organization.setOwner(owner);
        entityManager.persist(organization);

        Project alpha = new Project();
        alpha.setName("Alpha");
        alpha.setDescription("First");
        alpha.setOrganization(organization);
        entityManager.persist(alpha);

        Project beta = new Project();
        beta.setName("Beta");
        beta.setDescription("Second");
        beta.setOrganization(organization);
        entityManager.persist(beta);

        Project gamma = new Project();
        gamma.setName("Gamma");
        gamma.setDescription("Third");
        gamma.setOrganization(organization);
        entityManager.persist(gamma);

        entityManager.flush();

        Page<Project> page = projectRepository.findByOrganizationId(
                organization.getId(),
                PageRequest.of(0, 2, Sort.by("name").ascending())
        );

        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        assertEquals("Alpha", page.getContent().get(0).getName());
        assertEquals("Beta", page.getContent().get(1).getName());

        Page<Project> nextPage = projectRepository.findByOrganizationId(
                organization.getId(),
                PageRequest.of(1, 2, Sort.by("name").ascending())
        );

        assertEquals(1, nextPage.getContent().size());
        assertEquals("Gamma", nextPage.getContent().get(0).getName());
    }
}
