package com.gymmanagement.supplement;

import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.supplement.dto.SupplementResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class SupplementMapper {

    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "clientName", expression = "java(supplement.getClient().getUser().getFullName())")
    @Mapping(target = "dieticianId", source = "dietician.id")
    public abstract SupplementResponse toResponse(Supplement supplement);
}
