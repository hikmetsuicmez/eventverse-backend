package com.hikmetsuicmez.eventverse.event;

import com.hikmetsuicmez.eventverse.enums.ParticipantStatus;
import lombok.Getter;
import java.util.UUID;

@Getter
public class ParticipantStatusUpdateEvent {
    private final UUID eventId;
    private final UUID userId;
    private final ParticipantStatus status;

    public ParticipantStatusUpdateEvent(UUID eventId, UUID userId, ParticipantStatus status) {
        this.eventId = eventId;
        this.userId = userId;
        this.status = status;
    }
} 