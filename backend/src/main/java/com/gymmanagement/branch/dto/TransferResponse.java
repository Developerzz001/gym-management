package com.gymmanagement.branch.dto;

import java.time.LocalDateTime;

public record TransferResponse(Long id, Long subjectId, String subjectName, Long fromBranchId,
                               String fromBranchName, Long toBranchId, String toBranchName,
                               String reason, String transferredBy, LocalDateTime transferredAt) {
}