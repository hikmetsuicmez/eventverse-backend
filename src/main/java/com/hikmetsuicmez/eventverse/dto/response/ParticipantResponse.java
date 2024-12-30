package com.hikmetsuicmez.eventverse.dto.response;

import com.hikmetsuicmez.eventverse.enums.ParticipantStatus;
import lombok.Data;
import java.util.UUID;

@Data
public class ParticipantResponse {
    private UUID id;
    private UserResponse user;
    private EventResponse event;
    private ParticipantStatus status;
} 