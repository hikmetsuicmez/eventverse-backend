package com.hikmetsuicmez.eventverse.dto.response;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class OrganizerResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String profilePicture;
} 