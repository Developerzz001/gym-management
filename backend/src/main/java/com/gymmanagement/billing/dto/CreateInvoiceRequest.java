package com.gymmanagement.billing.dto;

import com.gymmanagement.billing.InvoiceType;
import com.gymmanagement.billing.PaymentMethod;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateInvoiceRequest(
        @NotNull Long clientId,
        @NotNull InvoiceType invoiceType,
        @NotBlank @Size(max = 300) String description,
        @DecimalMin(value = "0.01") BigDecimal totalAmount,
        @NotNull @DecimalMin("0.00") BigDecimal tax,
        @NotNull @DecimalMin("0.00") BigDecimal discount,
        @NotNull @FutureOrPresent LocalDate dueDate,
        Long membershipPlanId,
        Long membershipDiscountId,
        LocalDate serviceStartDate,
        @Positive Integer sessionCount,
        LocalDate purchaseDate,
        @DecimalMin(value = "0.00") BigDecimal initialPaymentAmount,
        PaymentMethod paymentMethod,
        @Size(max = 120) String transactionReference,
        @Size(max = 500) String paymentRemarks) {
}