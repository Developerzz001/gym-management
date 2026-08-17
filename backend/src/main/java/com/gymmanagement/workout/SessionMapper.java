package com.gymmanagement.workout;

import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.workout.dto.SessionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class SessionMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", expression = "java(session.getClient().getUser().getFullName())")
    @Mapping(target = "coachId", source = "coach.id")
    @Mapping(target = "coachName", expression = "java(session.getCoach().getUser().getFullName())")
    public abstract SessionResponse toResponse(TrainingSession session);
}
