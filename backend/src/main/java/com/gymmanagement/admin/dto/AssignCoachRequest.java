package com.gymmanagement.admin.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssignCoachRequest {

    @NotNull(message = "Client id is required")
    private Long clientId;

    @NotNull(message = "Coach id is required")
    private Long coachId;
}
