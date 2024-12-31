package com.hikmetsuicmez.eventverse.mapper;

import com.hikmetsuicmez.eventverse.dto.response.NotificationResponse;
import com.hikmetsuicmez.eventverse.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(source = "event.title", target = "eventTitle")
    @Mapping(source = "timestamp", target = "timestamp")
    NotificationResponse toResponse(Notification notification);
} 