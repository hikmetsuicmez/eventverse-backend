package com.hikmetsuicmez.eventverse.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.UUID;

@Data
public class ParticipantRequest {
    @NotNull(message = "Kullanıcı ID boş olamaz")
    private UUID userId;

    @NotNull(message = "Etkinlik ID boş olamaz")
    private UUID eventId;
} 