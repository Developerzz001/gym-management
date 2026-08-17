package com.gymmanagement.client;

import com.gymmanagement.client.dto.ClientResponse;
import com.gymmanagement.coach.FitnessCoach;
import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.dietician.Dietician;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class ClientMapper {

    @Mapping(target = "id", source = "client.id")
    @Mapping(target = "userId", source = "client.user.id")
    @Mapping(target = "firstName", source = "client.user.firstName")
    @Mapping(target = "lastName", source = "client.user.lastName")
    @Mapping(target = "email", source = "client.user.email")
    @Mapping(target = "active", source = "client.user.active")
    @Mapping(target = "assignedCoachId", source = "client.assignedCoach.id")
    @Mapping(target = "assignedCoachName", expression = "java(coachName(client.getAssignedCoach()))")
    @Mapping(target = "assignedDieticianId", source = "client.assignedDietician.id")
    @Mapping(target = "assignedDieticianName", expression = "java(dieticianName(client.getAssignedDietician()))")
    public abstract ClientResponse toResponse(Client client);

    protected String coachName(FitnessCoach coach) {
        return coach == null ? null : coach.getUser().getFullName();
    }

    protected String dieticianName(Dietician dietician) {
        return dietician == null ? null : dietician.getUser().getFullName();
    }
}
