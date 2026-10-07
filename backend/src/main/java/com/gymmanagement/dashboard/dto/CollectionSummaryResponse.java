package com.gymmanagement.dashboard.dto;

import java.math.BigDecimal;

public record CollectionSummaryResponse(
        BigDecimal cash,
        BigDecimal card,
        BigDecimal upi,
        BigDecimal bankTransfer,
        BigDecimal cheque,
        BigDecimal other,
        BigDecimal total) {
}