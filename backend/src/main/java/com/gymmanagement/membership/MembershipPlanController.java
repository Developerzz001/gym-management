package com.gymmanagement.membership;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.membership.dto.MembershipPlanRequest;
import com.gymmanagement.membership.dto.MembershipPlanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/membership-plans")
@RequiredArgsConstructor
@Tag(name = "Membership Plans", description = "Admin manages membership plans")
public class MembershipPlanController {

    private final MembershipPlanService membershipPlanService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new membership plan")
    public ApiResponse<MembershipPlanResponse> create(@Valid @RequestBody MembershipPlanRequest request) {
        return ApiResponse.success("Membership plan created successfully", membershipPlanService.createPlan(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update a membership plan")
    public ApiResponse<MembershipPlanResponse> update(@PathVariable Long id, @Valid @RequestBody MembershipPlanRequest request) {
        return ApiResponse.success("Membership plan updated successfully", membershipPlanService.updatePlan(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a membership plan")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        membershipPlanService.deletePlan(id);
        return ApiResponse.message("Membership plan deleted successfully");
    }

    @GetMapping
    @Operation(summary = "List all membership plans")
    public ApiResponse<List<MembershipPlanResponse>> getAll() {
        return ApiResponse.success(membershipPlanService.getAllPlans());
    }
}
