package com.gymmanagement.branch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BranchSettingsRequest(@NotBlank String workingHours,
                                    @NotBlank @Size(max = 60) String timezone,
                                    String membershipRules,
                                    String notificationPreferences,
                                    String attendanceRules) {
}