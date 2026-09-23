package com.gymmanagement.client;

import com.gymmanagement.client.dto.ClientResponse;
import com.gymmanagement.coach.FitnessCoach;
import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.dietician.Dietician;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;

@Mapper(config = CentralMapperConfig.class)
public abstract class ClientMapper {

    @Mapping(target = "id", source = "client.id")
    @Mapping(target = "userId", source = "client.user.id")
    @Mapping(target = "organizationId", source = "client.user.organization.id")
    @Mapping(target = "branchId", source = "client.user.branch.id")
    @Mapping(target = "branchName", source = "client.user.branch.branchName")
    @Mapping(target = "firstName", source = "client.user.firstName")
    @Mapping(target = "lastName", source = "client.user.lastName")
    @Mapping(target = "email", source = "client.user.email")
    @Mapping(target = "active", source = "client.user.active")
    @Mapping(target = "membershipActive", expression = "java(hasActiveMembership(client))")
    @Mapping(target = "membershipAssigned", expression = "java(hasMembership(client))")
    @Mapping(target = "membershipStartDate", expression = "java(membershipStartDate(client))")
    @Mapping(target = "membershipEndDate", expression = "java(membershipEndDate(client))")
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

    protected boolean hasActiveMembership(Client client) {
        return hasMembership(client)
                && client.getMemberships().get(0).getStatus() == com.gymmanagement.membership.MembershipStatus.ACTIVE
                && !client.getMemberships().get(0).getEndDate().isBefore(LocalDate.now());
    }

    protected boolean hasMembership(Client client) {
        return client.getMemberships() != null && !client.getMemberships().isEmpty();
    }

    protected LocalDate membershipStartDate(Client client) {
        return hasMembership(client) ? client.getMemberships().get(0).getStartDate() : null;
    }

    protected LocalDate membershipEndDate(Client client) {
        return hasMembership(client) ? client.getMemberships().get(0).getEndDate() : null;
    }
}
