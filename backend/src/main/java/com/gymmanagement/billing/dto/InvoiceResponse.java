package com.gymmanagement.billing.dto;

import com.gymmanagement.billing.*;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public record InvoiceResponse(
        Long id,
        String invoiceNumber,
        Long clientId,
        String clientName,
        InvoiceType invoiceType,
        Long membershipPlanId,
        String membershipPlanName,
        Long membershipDiscountId,
        String membershipDiscountName,
        BigDecimal membershipDiscountPercentage,
        Long generatedMembershipId,
        LocalDate serviceStartDate,
        Integer sessionCount,
        LocalDate purchaseDate,
        String description,
        BigDecimal totalAmount,
        BigDecimal tax,
        BigDecimal discount,
        BigDecimal finalAmount,
        BigDecimal amountPaid,
        BigDecimal balanceAmount,
        InvoiceStatus status,
        LocalDate dueDate,
        LocalDateTime createdAt,
        String createdBy,
        List<PaymentTransactionResponse> paymentHistory) {
}