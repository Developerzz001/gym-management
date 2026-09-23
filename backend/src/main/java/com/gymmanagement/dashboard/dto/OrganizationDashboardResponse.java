package com.gymmanagement.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record OrganizationDashboardResponse(Long organizationId, String organizationName, long totalBranches,
                                             long totalMembers, BigDecimal totalRevenue,
                                             List<BranchPerformanceResponse> branches) {
}