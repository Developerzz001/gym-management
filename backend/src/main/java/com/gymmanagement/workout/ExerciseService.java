package com.gymmanagement.workout;

import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.workout.dto.ExerciseRequest;
import com.gymmanagement.workout.dto.ExerciseResponse;

public interface ExerciseService {

    ExerciseResponse createExercise(ExerciseRequest request);

    ExerciseResponse updateExercise(Long id, ExerciseRequest request);

    void deleteExercise(Long id);

    ExerciseResponse getExerciseById(Long id);

    PageResponse<ExerciseResponse> getExercises(ExerciseCategory category, String name, int page, int size);

    Exercise getExerciseEntityById(Long id);
}
