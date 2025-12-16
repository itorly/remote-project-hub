package com.itorly.rph.organization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itorly.rph.organization.dto.CreateOrganizationRequest;
import com.itorly.rph.organization.dto.OrganizationResponse;
import com.itorly.rph.security.CustomUserDetailsService;
import com.itorly.rph.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OrganizationController.class,
        excludeAutoConfiguration = { SecurityAutoConfiguration.class }
)
@AutoConfigureMockMvc(addFilters = false)
class OrganizationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrganizationService organizationService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void createOrganization_returnsOrganizationResponse() throws Exception {
        CreateOrganizationRequest request = new CreateOrganizationRequest();
        request.setName("New Org");
        request.setDescription("Org description");

        OrganizationResponse response = new OrganizationResponse(
                10L,
                "New Org",
                "Org description",
                OrganizationRole.OWNER
        );

        when(organizationService.createOrganization(any(CreateOrganizationRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/organizations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.name").value("New Org"))
                .andExpect(jsonPath("$.description").value("Org description"))
                .andExpect(jsonPath("$.role").value("OWNER"));
    }

    @Test
    void getMyOrganizations_returnsOrganizationsForUser() throws Exception {
        OrganizationResponse org1 = new OrganizationResponse(
                10L,
                "Org One",
                "First org",
                OrganizationRole.OWNER
        );
        OrganizationResponse org2 = new OrganizationResponse(
                20L,
                "Org Two",
                "Second org",
                OrganizationRole.MEMBER
        );

        when(organizationService.getMyOrganizations())
                .thenReturn(List.of(org1, org2));

        mockMvc.perform(get("/api/organizations"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].name").value("Org One"))
                .andExpect(jsonPath("$[0].role").value("OWNER"))
                .andExpect(jsonPath("$[1].id").value(20L))
                .andExpect(jsonPath("$[1].name").value("Org Two"))
                .andExpect(jsonPath("$[1].role").value("MEMBER"));
    }
}
