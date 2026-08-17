package com.gymmanagement.workout.dto;

import com.gymmanagement.workout.ExerciseCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExerciseResponse {

    private Long id;
    private String name;
    private ExerciseCategory category;
    private String description;
}
