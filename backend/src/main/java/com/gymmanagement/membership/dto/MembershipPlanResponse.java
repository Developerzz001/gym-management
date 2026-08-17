package com.gymmanagement.membership.dto;

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
public class MembershipPlanResponse {

    private Long id;
    private String name;
    private Integer durationDays;
    private BigDecimal fees;
    private String description;
}
