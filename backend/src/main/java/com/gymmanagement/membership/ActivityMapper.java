package com.gymmanagement.membership;

import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.membership.dto.ActivityResponse;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface ActivityMapper {

    ActivityResponse toResponse(Activity activity);
}
