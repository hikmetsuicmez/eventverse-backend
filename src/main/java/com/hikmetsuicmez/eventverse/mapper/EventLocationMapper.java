package com.hikmetsuicmez.eventverse.mapper;

import com.hikmetsuicmez.eventverse.dto.response.EventLocationResponse;
import com.hikmetsuicmez.eventverse.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventLocationMapper {
    @Mapping(source = "location", target = "address")
    EventLocationResponse toLocationResponse(Event event);
} 