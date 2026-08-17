package com.gymmanagement.membership.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPlanRequest {

    @NotBlank(message = "Plan name is required")
    private String name;

    @NotNull(message = "Duration in days is required")
    @Positive(message = "Duration must be positive")
    private Integer durationDays;

    @NotNull(message = "Fees is required")
    @Positive(message = "Fees must be positive")
    private BigDecimal fees;

    private String description;
}
