package com.hikmetsuicmez.eventverse.mapper;

import com.hikmetsuicmez.eventverse.dto.request.EventRequest;
import com.hikmetsuicmez.eventverse.dto.response.EventResponse;
import com.hikmetsuicmez.eventverse.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EventMapper {
    Event toEntity(EventRequest request);
    EventResponse toResponse(Event event);
} 