package com.gymmanagement.membership;

import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.membership.dto.MembershipPlanResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface MembershipPlanMapper {

    @Mapping(target = "activityId", source = "plan.activity.id")
    @Mapping(target = "activityName", source = "plan.activity.name")
    MembershipPlanResponse toResponse(MembershipPlan plan);
}
