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
    @Mapping(target = "ageLimit", source = "ageLimit")
    @Mapping(target = "hasAgeLimit", source = "hasAgeLimit")
    @Mapping(target = "isPaid", source = "paid")
    @Mapping(target = "price", source = "price")
    @Mapping(target = "eventImage", source = "eventImage")
    @Mapping(target = "city", source = "coordinates.city")
    @Mapping(target = "country", source = "coordinates.country")
    @Mapping(target = "latitude", source = "coordinates.latitude")
    @Mapping(target = "longitude", source = "coordinates.longitude")
    Event toEntity(EventRequest eventRequest);

    @Mapping(source = "ageLimit", target = "ageLimit")
    @Mapping(source = "hasAgeLimit", target = "hasAgeLimit")
    @Mapping(source = "paid", target = "isPaid")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "eventImage", target = "eventImage")
    @Mapping(source = "city", target = "city")
    @Mapping(source = "country", target = "country")
    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    EventResponse toResponse(Event event);

    List<EventResponse> toResponseList(List<Event> events);
} 