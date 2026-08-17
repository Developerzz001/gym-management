package com.gymmanagement.membership.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignMembershipRequest {

    @NotNull(message = "Client id is required")
    private Long clientId;

    @NotNull(message = "Membership plan id is required")
    private Long membershipPlanId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;
}
