package com.gymmanagement.client;

import com.gymmanagement.client.dto.FollowUpResponse;
import com.gymmanagement.common.mapper.CentralMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public abstract class FollowUpMapper {

    @Mapping(target = "executiveId", source = "executive.id")
    @Mapping(target = "executiveName", expression = "java(executiveName(followUp))")
    public abstract FollowUpResponse toResponse(FollowUp followUp);

    protected String executiveName(FollowUp followUp) {
        return followUp.getExecutive() == null ? null : followUp.getExecutive().getFullName();
    }
}
