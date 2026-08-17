package com.gymmanagement.workout;

import com.gymmanagement.workout.dto.WorkoutPlanRequest;
import com.gymmanagement.workout.dto.WorkoutPlanResponse;

import java.util.List;

public interface WorkoutPlanService {

    WorkoutPlanResponse createWorkoutPlan(String coachEmail, WorkoutPlanRequest request);

    WorkoutPlanResponse updateWorkoutPlan(String coachEmail, Long planId, WorkoutPlanRequest request);

    void deleteWorkoutPlan(String coachEmail, Long planId);

    WorkoutPlanResponse getWorkoutPlanById(Long planId);

    List<WorkoutPlanResponse> getPlansByClient(Long clientId);

    List<WorkoutPlanResponse> getPlansByCoach(String coachEmail);

    WorkoutPlanResponse getTodaysWorkoutForClient(Long clientId);
}
