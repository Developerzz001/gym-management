package com.gymmanagement.dietician;

import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.dietician.dto.DieticianResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface DieticianMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "mobileNumber", source = "user.mobileNumber")
    @Mapping(target = "active", source = "user.active")
    DieticianResponse toResponse(Dietician dietician);
}
