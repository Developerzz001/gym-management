package com.gymmanagement.progress;

import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.progress.dto.ProgressRecordResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface ProgressRecordMapper {

    @Mapping(target = "clientId", source = "client.id")
    ProgressRecordResponse toResponse(ProgressRecord record);
}
