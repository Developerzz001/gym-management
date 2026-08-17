package com.gymmanagement.workout;

import com.gymmanagement.common.dto.ApiResponse;
import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.workout.dto.ExerciseRequest;
import com.gymmanagement.workout.dto.ExerciseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/exercises")
@RequiredArgsConstructor
@Tag(name = "Exercise Master", description = "Exercise catalog managed by fitness coaches")
public class ExerciseController {

    private final ExerciseService exerciseService;

    @PostMapping
    @PreAuthorize("hasRole('FITNESS_COACH')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new master exercise")
    public ApiResponse<ExerciseResponse> createExercise(@Valid @RequestBody ExerciseRequest request) {
        return ApiResponse.success("Exercise created successfully", exerciseService.createExercise(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('FITNESS_COACH')")
    @Operation(summary = "Update a master exercise")
    public ApiResponse<ExerciseResponse> updateExercise(@PathVariable Long id, @Valid @RequestBody ExerciseRequest request) {
        return ApiResponse.success("Exercise updated successfully", exerciseService.updateExercise(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('FITNESS_COACH')")
    @Operation(summary = "Delete a master exercise")
    public ApiResponse<Void> deleteExercise(@PathVariable Long id) {
        exerciseService.deleteExercise(id);
        return ApiResponse.message("Exercise deleted successfully");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get exercise by id")
    public ApiResponse<ExerciseResponse> getExercise(@PathVariable Long id) {
        return ApiResponse.success(exerciseService.getExerciseById(id));
    }

    @GetMapping
    @Operation(summary = "List / search exercises, optionally filter by category or name")
    public ApiResponse<PageResponse<ExerciseResponse>> getExercises(
            @RequestParam(required = false) ExerciseCategory category,
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return ApiResponse.success(exerciseService.getExercises(category, name, page, size));
    }
}
