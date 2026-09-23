package com.gymmanagement.dashboard;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.dashboard.dto.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1/dashboards")
@RequiredArgsConstructor
@Tag(name = "Multi-branch Dashboards", description = "Authorized branch and organization KPI aggregates")
public class MultiBranchDashboardController {
    private final MultiBranchDashboardService service;

    @GetMapping("/branches/{branchId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','BRANCH_MANAGER')")
    public ApiResponse<BranchDashboardResponse> branch(@PathVariable Long branchId) {
        return ApiResponse.success(service.branch(branchId));
    }

    @GetMapping("/organizations/{organizationId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ORGANIZATION_ADMIN')")
    public ApiResponse<OrganizationDashboardResponse> organization(@PathVariable Long organizationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponse.success(service.organization(organizationId, from, to));
    }
}