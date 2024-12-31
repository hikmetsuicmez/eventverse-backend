package com.hikmetsuicmez.eventverse.mapper;

import com.hikmetsuicmez.eventverse.dto.response.UserResponse;
import com.hikmetsuicmez.eventverse.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
} 