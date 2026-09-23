package com.gymmanagement.user;

import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.user.dto.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = CentralMapperConfig.class)
public interface UserMapper {

    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationName", source = "organization.name")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.branchName")
    UserResponse toResponse(User user);
}
