package com.hikmetsuicmez.eventverse.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private UUID id;
    private String title;
    private String description;
    private LocalDate date;
    private String location;
    private int maxParticipants;
    private String category;
    private OrganizerResponse organizer;
} 