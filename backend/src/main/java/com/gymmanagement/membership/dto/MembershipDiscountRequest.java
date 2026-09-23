package com.gymmanagement.membership.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MembershipDiscountRequest(
        @NotBlank @Size(max = 150) String name,
        @NotNull
                @DecimalMin(value = "0.00", message = "Discount percentage cannot be negative")
        @DecimalMax(value = "100.00", inclusive = false, message = "Discount percentage must be less than 100")
        BigDecimal percentage,
        @NotNull @Min(value = 0, message = "Extra free days cannot be negative") Integer extraFreeDays,
        @Size(max = 500) String description,
        boolean active) {

        @AssertTrue(message = "Provide either a discount percentage or extra free days, not both")
        public boolean isSingleBenefitProvided() {
                boolean hasPercentage = percentage != null && percentage.signum() > 0;
                boolean hasExtraFreeDays = extraFreeDays != null && extraFreeDays > 0;
                return hasPercentage != hasExtraFreeDays;
        }
}