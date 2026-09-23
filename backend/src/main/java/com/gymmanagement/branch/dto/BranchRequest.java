package com.gymmanagement.branch.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record BranchRequest(
        @NotNull Long organizationId,
        @NotBlank @Size(max = 150) String branchName,
        @NotBlank @Size(max = 500) String address,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String state,
        @NotBlank @Size(max = 100) String country,
        @NotBlank @Size(max = 20) String pincode,
        @Size(max = 20) String contactNumber,
        @Email @Size(max = 150) String email,
        Long managerId) {
}