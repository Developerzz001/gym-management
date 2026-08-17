package com.gymmanagement.user;

import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.user.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface UserMapper {

    UserResponse toResponse(User user);
}
