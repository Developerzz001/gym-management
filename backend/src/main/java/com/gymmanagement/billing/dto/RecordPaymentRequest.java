package com.gymmanagement.billing.dto;

import com.gymmanagement.billing.PaymentMethod;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RecordPaymentRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull PaymentMethod paymentMethod,
        @Size(max = 120) String transactionReference,
        @Size(max = 500) String remarks) {
}