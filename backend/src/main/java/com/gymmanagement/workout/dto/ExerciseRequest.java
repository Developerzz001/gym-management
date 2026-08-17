package com.gymmanagement.workout.dto;

import com.gymmanagement.workout.ExerciseCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ExerciseRequest {

    @NotBlank(message = "Exercise name is required")
    private String name;

    @NotNull(message = "Category is required")
    private ExerciseCategory category;

    private String description;
}
