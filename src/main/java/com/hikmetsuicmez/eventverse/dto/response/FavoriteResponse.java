package com.hikmetsuicmez.eventverse.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FavoriteResponse {

    private UUID id;
    private UUID userId;
    private UUID eventId;
    private LocalDateTime createdAt;
    private String eventTitle;
    private String eventDescription;
    private LocalDate eventDate;
    private String eventImageUrl;
}
