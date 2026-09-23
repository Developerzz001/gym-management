package com.gymmanagement.branch.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransferRequest(@NotNull Long destinationBranchId,
                              @NotBlank @Size(max = 500) String reason) {
}