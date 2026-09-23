package com.gymmanagement.dashboard;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.dashboard.dto.AdminDashboardResponse;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Business Dashboard", description = "Revenue, membership, attendance and staff KPIs")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/admin")
    @Operation(summary = "Get optimized admin business KPIs")
    public ApiResponse<AdminDashboardResponse> admin() {
        return ApiResponse.success(dashboardService.adminDashboard());
    }
}