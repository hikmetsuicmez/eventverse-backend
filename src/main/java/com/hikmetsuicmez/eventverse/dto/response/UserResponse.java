package com.hikmetsuicmez.eventverse.dto.response;

import lombok.Data;
import java.util.UUID;
import com.hikmetsuicmez.eventverse.enums.UserRole;

@Data
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private UserRole role;
} 