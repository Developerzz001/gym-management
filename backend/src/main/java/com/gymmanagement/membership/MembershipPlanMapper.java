package com.gymmanagement.membership;

import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.membership.dto.MembershipPlanResponse;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface MembershipPlanMapper {

    MembershipPlanResponse toResponse(MembershipPlan plan);
}
