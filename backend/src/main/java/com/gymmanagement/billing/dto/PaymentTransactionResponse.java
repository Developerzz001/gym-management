package com.gymmanagement.billing.dto;

import com.gymmanagement.billing.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentTransactionResponse(
        Long id,
        BigDecimal paidAmount,
        TransactionType transactionType,
        PaymentStatus status,
        PaymentMethod paymentMethod,
        String transactionReference,
        String remarks,
        LocalDateTime paymentDate,
        String createdBy) {
}