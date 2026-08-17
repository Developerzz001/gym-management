package com.gymmanagement.workout.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionRequest {

    @NotNull(message = "Client id is required")
    private Long clientId;

    @NotNull(message = "Session date/time is required")
    @Future(message = "Session date/time must be in the future")
    private LocalDateTime sessionDateTime;

    private String notes;
}
