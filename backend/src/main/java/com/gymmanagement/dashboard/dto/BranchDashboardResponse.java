package com.gymmanagement.dashboard.dto;

import java.math.BigDecimal;

public record BranchDashboardResponse(Long branchId, String branchName, long totalMembers, long activeMembers,
                                      long newMembers, long expiringMemberships, BigDecimal dailyRevenue,
                                      BigDecimal monthlyRevenue, long attendanceToday, long activeCoaches,
                                      long activeDieticians) {
}