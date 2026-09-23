package com.gymmanagement.branch.dto;

public record BranchSettingsResponse(Long branchId, String workingHours, String timezone, String membershipRules,
                                     String notificationPreferences, String attendanceRules) {
}