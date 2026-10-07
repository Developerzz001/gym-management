package com.gymmanagement.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record DashboardRecordResponse(
        Long id,
        String name,
        String mobileNumber,
        String sportActivity,
        String membershipPlan,
        LocalDate date,
        Long remainingDays,
        BigDecimal pendingAmount,
        LocalDate dueDate,
        LocalDateTime appointmentTime,
        String followUpType,
        String comment,
        String doneBy,
        LocalDate nextFollowUpDate,
        Long clientId) {
}