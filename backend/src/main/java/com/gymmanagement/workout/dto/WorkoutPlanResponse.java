package com.gymmanagement.workout.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanResponse {

    private Long id;
    private Long clientId;
    private String clientName;
    private Long coachId;
    private String coachName;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private List<WorkoutPlanDetailResponse> details;
}
