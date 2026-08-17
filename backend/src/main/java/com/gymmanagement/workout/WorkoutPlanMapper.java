package com.gymmanagement.workout;

import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.workout.dto.WorkoutPlanDetailResponse;
import com.gymmanagement.workout.dto.WorkoutPlanResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class WorkoutPlanMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", expression = "java(plan.getClient().getUser().getFullName())")
    @Mapping(target = "coachId", source = "coach.id")
    @Mapping(target = "coachName", expression = "java(plan.getCoach().getUser().getFullName())")
    public abstract WorkoutPlanResponse toResponse(WorkoutPlan plan);

    @Mapping(target = "exerciseId", source = "exercise.id")
    @Mapping(target = "exerciseName", source = "exercise.name")
    @Mapping(target = "exerciseCategory", source = "exercise.category")
    public abstract WorkoutPlanDetailResponse toDetailResponse(WorkoutPlanDetail detail);

    public abstract List<WorkoutPlanDetailResponse> toDetailResponseList(List<WorkoutPlanDetail> details);
}
