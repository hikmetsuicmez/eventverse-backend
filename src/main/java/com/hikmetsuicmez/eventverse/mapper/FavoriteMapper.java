package com.hikmetsuicmez.eventverse.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.hikmetsuicmez.eventverse.dto.request.FavoriteRequest;
import com.hikmetsuicmez.eventverse.dto.response.FavoriteResponse;
import com.hikmetsuicmez.eventverse.entity.Favorite;

@Mapper(componentModel = "spring")
public interface FavoriteMapper {

    @Mapping(source = "eventId", target = "event.id")
    Favorite toEntity(FavoriteRequest favoriteRequest);

    @Mapping(source = "event.id", target = "eventId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "event.title", target = "eventTitle")
    @Mapping(source = "event.description", target = "eventDescription")
    @Mapping(source = "event.date", target = "eventDate")
    FavoriteResponse toResponse(Favorite favorite);

}
