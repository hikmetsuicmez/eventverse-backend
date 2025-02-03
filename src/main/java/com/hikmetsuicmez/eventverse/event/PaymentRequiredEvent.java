package com.hikmetsuicmez.eventverse.event;

import lombok.Getter;
import java.util.UUID;

@Getter
public class PaymentRequiredEvent {
    private final UUID eventId;
    private final UUID userId;

    public PaymentRequiredEvent(UUID eventId, UUID userId) {
        this.eventId = eventId;
        this.userId = userId;
    }
} 