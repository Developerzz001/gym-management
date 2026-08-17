package com.gymmanagement.workout.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkoutPlanRequest {

    @NotNull(message = "Client id is required")
    private Long clientId;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotEmpty(message = "At least one workout detail entry is required")
    @Valid
    private List<WorkoutPlanDetailRequest> details;
}
