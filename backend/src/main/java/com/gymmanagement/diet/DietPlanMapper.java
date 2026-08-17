package com.gymmanagement.diet;

import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.diet.dto.DietPlanDetailResponse;
import com.gymmanagement.diet.dto.DietPlanResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = CentralMapperConfig.class)
public abstract class DietPlanMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", expression = "java(plan.getClient().getUser().getFullName())")
    @Mapping(target = "dieticianId", source = "dietician.id")
    @Mapping(target = "dieticianName", expression = "java(plan.getDietician().getUser().getFullName())")
    public abstract DietPlanResponse toResponse(DietPlan plan);

    public abstract DietPlanDetailResponse toDetailResponse(DietPlanDetail detail);

    public abstract List<DietPlanDetailResponse> toDetailResponseList(List<DietPlanDetail> details);
}
