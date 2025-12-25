package com.itorly.rph.organization;

import com.itorly.rph.organization.dto.CreateOrganizationRequest;
import com.itorly.rph.organization.dto.OrganizationResponse;
import com.itorly.rph.organization.dto.UpdateOrganizationRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organizations")
@Tag(name = "Organization Management", description = "The CRUD operations of organizations.")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @PostMapping
    @Operation(summary = "Organization creation", description = "Create a new organization.")
    public ResponseEntity<OrganizationResponse> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request
    ) {
        OrganizationResponse response = organizationService.createOrganization(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Query all organizations", description = "Return all organizations")
    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getMyOrganizations() {
        List<OrganizationResponse> orgs = organizationService.getMyOrganizations();
        return ResponseEntity.ok(orgs);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Organization update", description = "Update the specified organization based on the ID.")
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrganizationRequest request
    ) {
        OrganizationResponse updated = organizationService.updateOrganization(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Organization deletion", description = "Delete the specified organization based on the ID.")
    public ResponseEntity<Void> deleteOrganization(@PathVariable Long id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }
}
