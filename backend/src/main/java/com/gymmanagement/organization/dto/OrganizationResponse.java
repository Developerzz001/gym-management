package com.gymmanagement.organization.dto;

import com.gymmanagement.organization.OrganizationStatus;
import java.time.LocalDateTime;

public record OrganizationResponse(Long id, String code, String name, String ownerName, String contactNumber,
                                   String email, String address, OrganizationStatus status,
                                   LocalDateTime createdDate, LocalDateTime updatedDate) {
}