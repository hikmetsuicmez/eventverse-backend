package com.hikmetsuicmez.eventverse.dto.response;

import java.time.LocalDate;
import java.util.UUID;

import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventFilterResponse {

    private UUID id;
    private String title;
    private String description;
    private String category;
    private String location;
    private LocalDate date;
    private Double price;
    private Integer ageLimit;
    private Boolean isPaid;
    private Boolean hasAgeLimit;
    private String imageUrl;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private UserResponse organizer;

    public EventFilterResponse(Event event) {
        this.id = event.getId();
        this.title = event.getTitle();
        this.description = event.getDescription();
        this.category = event.getCategory();
        this.location = event.getLocation();
        this.date = event.getDate();
        this.price = event.getPrice();
        this.ageLimit = event.getAgeLimit();
        this.isPaid = event.isPaid();
        this.hasAgeLimit = event.isHasAgeLimit();
        this.imageUrl = event.getImageUrl();
        this.maxParticipants = event.getMaxParticipants();
        this.currentParticipants = event.getParticipants() != null ? event.getParticipants().size() : 0;
        
        User organizer = event.getOrganizer();
        if (organizer != null) {
            this.organizer = new UserResponse(
                organizer.getId(),
                organizer.getFirstName(),
                organizer.getLastName(),
                organizer.getEmail(),
                organizer.getPhoneNumber(),
                organizer.getAddress(),
                organizer.getProfilePicture(),
                organizer.getBirthDate()
            );
        }
    }
}
