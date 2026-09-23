package com.gymmanagement.membership.dto;

import java.math.BigDecimal;

public record MembershipDiscountResponse(
        Long id,
        String name,
        BigDecimal percentage,
        Integer extraFreeDays,
        String description,
        boolean active) {
}