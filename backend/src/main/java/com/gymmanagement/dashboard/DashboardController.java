package com.gymmanagement.dashboard;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.dashboard.dto.AdminDashboardResponse;
import com.gymmanagement.dashboard.dto.DashboardRecordResponse;
import com.gymmanagement.dashboard.dto.OperationalDashboardSummaryResponse;
import com.gymmanagement.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Business Dashboard", description = "Revenue, membership, attendance and staff KPIs")
public class DashboardController {

    private final DashboardService dashboardService;
    private final OperationalDashboardService operationalDashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get optimized admin business KPIs")
    public ApiResponse<AdminDashboardResponse> admin() {
        return ApiResponse.success(dashboardService.adminDashboard());
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','ORGANIZATION_ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    public ApiResponse<OperationalDashboardSummaryResponse> summary(@RequestParam(required = false) Long branchId) {
        return ApiResponse.success(operationalDashboardService.summary(branchId));
    }

    @GetMapping({"/member-birthdays", "/staff-birthdays", "/inquiry-followups", "/renewal-followups",
            "/membership-expiring", "/done-followups", "/balance-payments", "/appointments"})
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','ORGANIZATION_ADMIN','BRANCH_MANAGER','RECEPTIONIST')")
    public ApiResponse<PageResponse<DashboardRecordResponse>> details(
            jakarta.servlet.http.HttpServletRequest request,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "date") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) Long branchId) {
        DashboardCategory category = category(request.getRequestURI());
        String sortProperty = switch (sort) {
            case "name", "mobileNumber", "date" -> sort;
            case "pendingAmount" -> category == DashboardCategory.BALANCE_PAYMENTS ? sort : "date";
            case "dueDate" -> category == DashboardCategory.BALANCE_PAYMENTS ? sort : "date";
            case "appointmentTime" -> category == DashboardCategory.APPOINTMENTS ? sort : "date";
            default -> "date";
        };
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;
        var result = operationalDashboardService.details(category, keyword, branchId,
                PageRequest.of(Math.max(0, page), Math.min(100, Math.max(1, size)), Sort.by(sortDirection, sortProperty)));
        return ApiResponse.success(PageResponse.from(result));
    }

    private DashboardCategory category(String uri) {
        String endpoint = uri.substring(uri.lastIndexOf('/') + 1);
        return switch (endpoint) {
            case "member-birthdays" -> DashboardCategory.MEMBER_BIRTHDAYS;
            case "staff-birthdays" -> DashboardCategory.STAFF_BIRTHDAYS;
            case "inquiry-followups" -> DashboardCategory.INQUIRY_FOLLOWUPS;
            case "renewal-followups" -> DashboardCategory.RENEWAL_FOLLOWUPS;
            case "membership-expiring" -> DashboardCategory.MEMBERSHIP_EXPIRING;
            case "done-followups" -> DashboardCategory.DONE_FOLLOWUPS;
            case "balance-payments" -> DashboardCategory.BALANCE_PAYMENTS;
            case "appointments" -> DashboardCategory.APPOINTMENTS;
            default -> throw new IllegalArgumentException("Unsupported dashboard endpoint");
        };
    }
}