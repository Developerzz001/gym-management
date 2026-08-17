package com.gymmanagement.workout.dto;

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
public class WorkoutPlanDetailResponse {

    private Long id;
    private DayOfWeek dayOfWeek;
    private Long exerciseId;
    private String exerciseName;
    private String exerciseCategory;
    private Integer sets;
    private Integer reps;
    private Integer durationMinutes;
    private Integer restTimeSeconds;
    private String notes;
}
