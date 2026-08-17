package com.gymmanagement.workout.dto;

import com.gymmanagement.workout.SessionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SessionStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private SessionStatus status;
}
