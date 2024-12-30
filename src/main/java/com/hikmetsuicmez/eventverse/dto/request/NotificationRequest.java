package com.hikmetsuicmez.eventverse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.UUID;

@Data
public class NotificationRequest {
    @NotNull(message = "Alıcı ID boş olamaz")
    private UUID recipientId;

    @NotNull(message = "Etkinlik ID boş olamaz")
    private UUID eventId;

    @NotBlank(message = "Mesaj boş olamaz")
    @Size(max = 500, message = "Mesaj en fazla 500 karakter olabilir")
    private String message;
} 