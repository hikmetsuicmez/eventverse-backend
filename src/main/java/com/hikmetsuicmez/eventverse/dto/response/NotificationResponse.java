package com.hikmetsuicmez.eventverse.dto.response;

import com.hikmetsuicmez.eventverse.enums.NotificationStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {
    private UUID id;
    private String message;
    private NotificationStatus status;
    private LocalDateTime timestamp;
    private String eventTitle;
    private UUID eventId;
} 