package com.gymmanagement.workout;

import com.gymmanagement.common.dto.PageResponse;
import com.gymmanagement.common.exception.ResourceNotFoundException;
import com.gymmanagement.workout.dto.ExerciseRequest;
import com.gymmanagement.workout.dto.ExerciseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;

    @Override
    @Transactional
    public ExerciseResponse createExercise(ExerciseRequest request) {
        Exercise exercise = Exercise.builder()
                .name(request.getName())
                .category(request.getCategory())
                .description(request.getDescription())
                .build();
        return exerciseMapper.toResponse(exerciseRepository.save(exercise));
    }

    @Override
    @Transactional
    public ExerciseResponse updateExercise(Long id, ExerciseRequest request) {
        Exercise exercise = getExerciseEntityById(id);
        exercise.setName(request.getName());
        exercise.setCategory(request.getCategory());
        exercise.setDescription(request.getDescription());
        return exerciseMapper.toResponse(exerciseRepository.save(exercise));
    }

    @Override
    @Transactional
    public void deleteExercise(Long id) {
        exerciseRepository.delete(getExerciseEntityById(id));
    }

    @Override
    public ExerciseResponse getExerciseById(Long id) {
        return exerciseMapper.toResponse(getExerciseEntityById(id));
    }

    @Override
    public PageResponse<ExerciseResponse> getExercises(ExerciseCategory category, String name, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        Page<Exercise> exercises;
        if (category != null) {
            exercises = exerciseRepository.findByCategory(category, pageable);
        } else if (name != null && !name.isBlank()) {
            exercises = exerciseRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            exercises = exerciseRepository.findAll(pageable);
        }
        return PageResponse.from(exercises.map(exerciseMapper::toResponse));
    }

    @Override
    public Exercise getExerciseEntityById(Long id) {
        return exerciseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", id));
    }
}
