package com.gymmanagement.workout.dto;

import com.gymmanagement.workout.SessionStatus;
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
public class SessionResponse {

    private Long id;
    private Long clientId;
    private String clientName;
    private Long coachId;
    private String coachName;
    private LocalDateTime sessionDateTime;
    private SessionStatus status;
    private String notes;
}
