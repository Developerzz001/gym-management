package com.gymmanagement.notification;

import com.gymmanagement.common.mapper.CentralMapperConfig;
import com.gymmanagement.notification.dto.NotificationResponse;
import org.mapstruct.Mapper;

@Mapper(config = CentralMapperConfig.class)
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
}
