package com.gymmanagement.dashboard.dto;

import java.math.BigDecimal;

public record BranchPerformanceResponse(Long branchId, String branchName, long members, BigDecimal revenue,
                                        long attendance, BigDecimal performanceScore) {
}