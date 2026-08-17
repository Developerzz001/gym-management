package com.gymmanagement.workout;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, Long> {

    List<WorkoutPlan> findByClientId(Long clientId);

    List<WorkoutPlan> findByCoachId(Long coachId);

    boolean existsByClientIdAndCoachId(Long clientId, Long coachId);
}
