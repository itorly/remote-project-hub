package com.itorly.rph.organization;

import com.itorly.rph.organization.dto.AddOrganizationMemberRequest;
import com.itorly.rph.organization.dto.OrganizationMemberResponse;
import com.itorly.rph.organization.dto.UpdateOrganizationMemberRoleRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/organizations/{organizationId}/members")
@Tag(name = "Organization Members", description = "Manage organization membership and roles.")
public class OrganizationMemberController {

    private final OrganizationMemberService memberService;

    public OrganizationMemberController(OrganizationMemberService memberService) {
        this.memberService = memberService;
    }

    @Operation(summary = "Add member", description = "Add a user to the organization (admin only).")
    @PostMapping
    public ResponseEntity<OrganizationMemberResponse> addMember(
            @PathVariable Long organizationId,
            @Valid @RequestBody AddOrganizationMemberRequest request
    ) {
        OrganizationMemberResponse response = memberService.addMember(organizationId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update member role", description = "Update a member's role (admin only).")
    @PatchMapping("/{memberId}")
    public ResponseEntity<OrganizationMemberResponse> updateMemberRole(
            @PathVariable Long organizationId,
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateOrganizationMemberRoleRequest request
    ) {
        OrganizationMemberResponse response = memberService.updateMemberRole(organizationId, memberId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Remove member", description = "Remove a member from the organization (admin only).")
    @DeleteMapping("/{memberId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long organizationId,
            @PathVariable Long memberId
    ) {
        memberService.removeMember(organizationId, memberId);
        return ResponseEntity.noContent().build();
    }
}
