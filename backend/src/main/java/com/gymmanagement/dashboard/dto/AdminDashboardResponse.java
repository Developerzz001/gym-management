package com.gymmanagement.dashboard.dto;

import com.gymmanagement.attendance.dto.PeakHourResponse;

import java.math.BigDecimal;
import java.util.List;

public record AdminDashboardResponse(
        BigDecimal totalRevenue,
        BigDecimal monthlyRevenue,
        BigDecimal todayRevenue,
        BigDecimal outstandingPayments,
        BigDecimal overduePayments,
        BigDecimal collectionEfficiencyPercent,
        long activeMembers,
        long newMembers,
        long renewals,
        long expiringMemberships,
        long todayAttendance,
        long monthlyAttendance,
        List<PeakHourResponse> peakUsageHours,
        long sessionsConducted,
        List<PerformanceResponse> topPerformingCoaches,
        List<PerformanceResponse> topPerformingDieticians) {
}