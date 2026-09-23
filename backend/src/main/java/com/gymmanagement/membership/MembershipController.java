package com.gymmanagement.membership;

import com.gymmanagement.common.dto.ApiResponse;
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
@PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
@Tag(name = "Memberships", description = "Assign and renew client memberships")
public class MembershipController {

    private final MembershipService membershipService;

    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CLIENT')")
    @Operation(summary = "Get membership history for a client")
    public ApiResponse<List<MembershipResponse>> getByClient(@PathVariable Long clientId) {
        return ApiResponse.success(membershipService.getByClient(clientId));
    }
}
