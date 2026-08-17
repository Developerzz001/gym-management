package com.gymmanagement.workout;

import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.workout.dto.ExerciseResponse;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface ExerciseMapper {

    ExerciseResponse toResponse(Exercise exercise);
}
