package com.hikmetsuicmez.eventverse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

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

    private String eventImage;

    private Integer ageLimit; // null olabilir, yaş sınırı yoksa null

    @Builder.Default
    private boolean hasAgeLimit = false; // Yaş sınırı var mı yok mu

    @Builder.Default
    private boolean isPaid = false; // Ücretli mi değil mi

    private Double price; // Ücretliyse kişi başı ücret (TL)
}