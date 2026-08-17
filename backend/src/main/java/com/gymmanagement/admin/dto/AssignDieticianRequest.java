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
public class AssignDieticianRequest {

    @NotNull(message = "Client id is required")
    private Long clientId;

    @NotNull(message = "Dietician id is required")
    private Long dieticianId;
}
