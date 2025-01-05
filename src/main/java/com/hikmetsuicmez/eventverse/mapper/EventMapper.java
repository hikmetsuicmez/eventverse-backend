package com.hikmetsuicmez.eventverse.mapper;

import com.hikmetsuicmez.eventverse.dto.request.EventRequest;
import com.hikmetsuicmez.eventverse.dto.response.EventResponse;
import com.hikmetsuicmez.eventverse.dto.response.OrganizerResponse;
import com.hikmetsuicmez.eventverse.dto.response.ParticipantResponse;
import com.hikmetsuicmez.eventverse.dto.response.UserResponse;
import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.Participant;
import com.hikmetsuicmez.eventverse.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

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
    @Mapping(source = "organizer", target = "organizer", qualifiedByName = "toOrganizerResponse")
    @Mapping(source = "participants", target = "participants", qualifiedByName = "toParticipantResponseList")
    EventResponse toResponse(Event event);

    List<EventResponse> toResponseList(List<Event> events);

    @Named("toOrganizerResponse")
    default OrganizerResponse toOrganizerResponse(User user) {
        if (user == null) {
            return null;
        }
        return OrganizerResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .build();
    }

    @Named("toParticipantResponseList")
    default List<ParticipantResponse> toParticipantResponseList(List<Participant> participants) {
        if (participants == null) {
            return null;
        }
        return participants.stream()
                .map(participant -> ParticipantResponse.builder()
                        .id(participant.getId())
                        .user(UserResponse.builder()
                                .id(participant.getUser().getId())
                                .firstName(participant.getUser().getFirstName())
                                .lastName(participant.getUser().getLastName())
                                .email(participant.getUser().getEmail())
                                .profilePicture(participant.getUser().getProfilePicture())
                                .build())
                        .status(participant.getStatus())
                        .registrationDate(participant.getRegistrationDate())
                        .build())
                .collect(Collectors.toList());
    }
} 