package com.gymmanagement.workout;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.workout.dto.WorkoutPlanRequest;
import com.gymmanagement.workout.dto.WorkoutPlanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/workout-plans")
@RequiredArgsConstructor
@Tag(name = "Workout Plans", description = "Coach creates/updates weekly workout plans for assigned clients")
public class WorkoutPlanController {

    private final WorkoutPlanService workoutPlanService;

    @PostMapping
    @PreAuthorize("hasRole('FITNESS_COACH')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new workout plan for an assigned client")
    public ApiResponse<WorkoutPlanResponse> createPlan(Authentication authentication,
                                                        @Valid @RequestBody WorkoutPlanRequest request) {
        return ApiResponse.success("Workout plan created successfully",
                workoutPlanService.createWorkoutPlan(authentication.getName(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FITNESS_COACH')")
    @Operation(summary = "Update an existing workout plan")
    public ApiResponse<WorkoutPlanResponse> updatePlan(Authentication authentication, @PathVariable Long id,
                                                        @Valid @RequestBody WorkoutPlanRequest request) {
        return ApiResponse.success("Workout plan updated successfully",
                workoutPlanService.updateWorkoutPlan(authentication.getName(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FITNESS_COACH')")
    @Operation(summary = "Delete a workout plan")
    public ApiResponse<Void> deletePlan(Authentication authentication, @PathVariable Long id) {
        workoutPlanService.deleteWorkoutPlan(authentication.getName(), id);
        return ApiResponse.message("Workout plan deleted successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get workout plan by id")
    public ApiResponse<WorkoutPlanResponse> getPlan(@PathVariable Long id) {
        return ApiResponse.success(workoutPlanService.getWorkoutPlanById(id));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Get all workout plans for a specific client")
    public ApiResponse<List<WorkoutPlanResponse>> getPlansByClient(@PathVariable Long clientId) {
        return ApiResponse.success(workoutPlanService.getPlansByClient(clientId));
    }

    @GetMapping("/client/{clientId}/today")
    @Operation(summary = "Get today's workout for a client")
    public ApiResponse<WorkoutPlanResponse> getTodaysWorkout(@PathVariable Long clientId) {
        return ApiResponse.success(workoutPlanService.getTodaysWorkoutForClient(clientId));
    }

    @GetMapping("/my-plans")
    @PreAuthorize("hasRole('FITNESS_COACH')")
    @Operation(summary = "Get all workout plans created by the currently logged-in coach")
    public ApiResponse<List<WorkoutPlanResponse>> getMyPlans(Authentication authentication) {
        return ApiResponse.success(workoutPlanService.getPlansByCoach(authentication.getName()));
    }
}
