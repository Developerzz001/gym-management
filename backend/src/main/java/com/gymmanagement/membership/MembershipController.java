package com.gymmanagement.membership;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.membership.dto.AssignMembershipRequest;
import com.gymmanagement.membership.dto.MembershipResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/memberships")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Memberships", description = "Assign and renew client memberships")
public class MembershipController {

    private final MembershipService membershipService;

    @PostMapping("/assign")
    @Operation(summary = "Assign a membership plan to a client")
    public ApiResponse<MembershipResponse> assign(@Valid @RequestBody AssignMembershipRequest request) {
        return ApiResponse.success("Membership assigned successfully", membershipService.assignMembership(request));
    }

    @PostMapping("/renew/{clientId}")
    @Operation(summary = "Renew a client's current membership")
    public ApiResponse<MembershipResponse> renew(@PathVariable Long clientId) {
        return ApiResponse.success("Membership renewed successfully", membershipService.renewMembership(clientId));
    }

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN','CLIENT')")
    @Operation(summary = "Get membership history for a client")
    public ApiResponse<List<MembershipResponse>> getByClient(@PathVariable Long clientId) {
        return ApiResponse.success(membershipService.getByClient(clientId));
    }
}
