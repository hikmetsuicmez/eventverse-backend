package com.hikmetsuicmez.eventverse.mapper;

import com.hikmetsuicmez.eventverse.dto.request.NotificationRequest;
import com.hikmetsuicmez.eventverse.dto.response.NotificationResponse;
import com.hikmetsuicmez.eventverse.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {
    Notification toEntity(NotificationRequest request);
    NotificationResponse toResponse(Notification notification);
} 