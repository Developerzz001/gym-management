package com.gymmanagement.coach;

import com.gymmanagement.coach.dto.CoachResponse;
import com.gymmanagement.common.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface CoachMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "mobileNumber", source = "user.mobileNumber")
    @Mapping(target = "active", source = "user.active")
    CoachResponse toResponse(FitnessCoach coach);
}
