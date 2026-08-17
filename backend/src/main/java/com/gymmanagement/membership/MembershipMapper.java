package com.gymmanagement.membership;

import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.membership.dto.MembershipResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class MembershipMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", expression = "java(membership.getClient().getUser().getFullName())")
    @Mapping(target = "membershipPlanId", source = "membershipPlan.id")
    @Mapping(target = "membershipPlanName", source = "membershipPlan.name")
    public abstract MembershipResponse toResponse(Membership membership);
}
