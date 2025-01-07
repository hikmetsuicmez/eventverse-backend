package com.hikmetsuicmez.eventverse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String title;
    private String description;
    private LocalDate date;
    private String location;
    private int maxParticipants;
    private String category;

    @ManyToOne
    @JoinColumn(name = "organizer_id")
    private User organizer;

    private Double latitude;
    private Double longitude;
    private String city;
    private String country;

    @Column(name = "age_limit")
    private Integer ageLimit;

    @Column(name = "has_age_limit")
    private boolean hasAgeLimit;

    @Column(name = "is_paid")
    private boolean isPaid;

    @Column(name = "price")
    private Double price;

    @Column(name = "event_time")
    private String eventTime;

    @Column(name = "event_image")
    private String eventImage;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Participant> participants;

}