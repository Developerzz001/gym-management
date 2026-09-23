package com.gymmanagement.billing.dto;

import jakarta.validation.constraints.Email;

public record ShareInvoiceRequest(@Email String email) {
}