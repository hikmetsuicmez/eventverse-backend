package com.hikmetsuicmez.eventverse.dto.response;

import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class EventResponse {
    private UUID id;
    private String title;
    private String description;
    private LocalDate date;
    private String location;
    private int maxParticipants;
    private String category;
    private UserResponse organizer;
} 