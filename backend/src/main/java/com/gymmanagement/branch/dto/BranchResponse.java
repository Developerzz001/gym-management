package com.gymmanagement.branch.dto;

import com.gymmanagement.branch.BranchStatus;
import java.time.LocalDateTime;

public record BranchResponse(Long id, Long organizationId, String organizationName, String branchCode,
                             String branchName, String address, String city, String state, String country,
                             String pincode, String contactNumber, String email, Long managerId,
                             String managerName, BranchStatus status, LocalDateTime createdDate,
                             LocalDateTime updatedDate) {
}