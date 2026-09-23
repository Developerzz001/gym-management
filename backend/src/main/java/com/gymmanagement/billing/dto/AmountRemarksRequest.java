package com.gymmanagement.billing.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record AmountRemarksRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank @Size(max = 500) String remarks) {
}