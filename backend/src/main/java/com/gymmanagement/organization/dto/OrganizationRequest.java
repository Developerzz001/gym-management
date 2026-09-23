package com.gymmanagement.organization.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrganizationRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Size(max = 150) String ownerName,
        @Size(max = 20) String contactNumber,
        @NotBlank @Email @Size(max = 150) String email,
        @Size(min = 6, max = 100) String adminPassword,
        @Size(max = 500) String address) {
}