package com.gymmanagement.dashboard.dto;

public record OperationalDashboardSummaryResponse(
        long memberBirthdays,
        long staffBirthdays,
        long inquiryFollowups,
        long renewalFollowups,
        long membershipExpiring,
        long doneFollowups,
        long balancePayments,
        long appointments,
        CollectionSummaryResponse collection) {
}