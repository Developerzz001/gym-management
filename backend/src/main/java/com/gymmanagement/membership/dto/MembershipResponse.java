package com.gymmanagement.membership.dto;

import com.gymmanagement.membership.MembershipStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipResponse {

    private Long id;
    private Long clientId;
    private String clientName;
    private Long membershipPlanId;
    private String membershipPlanName;
    private LocalDate startDate;
    private LocalDate endDate;
    private MembershipStatus status;
}
