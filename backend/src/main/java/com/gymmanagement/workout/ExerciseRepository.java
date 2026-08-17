package com.gymmanagement.workout;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    Page<Exercise> findByCategory(ExerciseCategory category, Pageable pageable);

    Page<Exercise> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
