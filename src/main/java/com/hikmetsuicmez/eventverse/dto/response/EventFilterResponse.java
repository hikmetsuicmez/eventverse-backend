package com.hikmetsuicmez.eventverse.dto.response;

import java.time.LocalDate;
import java.util.UUID;

import com.hikmetsuicmez.eventverse.entity.Event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
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
    }
}
