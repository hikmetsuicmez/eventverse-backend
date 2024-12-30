package com.hikmetsuicmez.eventverse.mapper;

import com.hikmetsuicmez.eventverse.dto.request.EventRequest;
import com.hikmetsuicmez.eventverse.dto.response.EventResponse;
import com.hikmetsuicmez.eventverse.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "organizer", ignore = true)
    Event toEntity(EventRequest request);

    EventResponse toResponse(Event event);

    List<EventResponse> toResponseList(List<Event> events);
} 