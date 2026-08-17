package com.gymmanagement.workout.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.DayOfWeek;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanDetailRequest {

    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;

    @NotNull(message = "Exercise is required")
    private Long exerciseId;

    private Integer sets;

    private Integer reps;

    private Integer durationMinutes;

    private Integer restTimeSeconds;

    private String notes;
}
