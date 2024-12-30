package com.hikmetsuicmez.eventverse.dto.response;

import com.hikmetsuicmez.eventverse.enums.NotificationStatus;
import lombok.Data;
import java.util.UUID;

@Data
public class NotificationResponse {
    private UUID id;
    private UserResponse recipient;
    private EventResponse event;
    private String message;
    private NotificationStatus status;
} 