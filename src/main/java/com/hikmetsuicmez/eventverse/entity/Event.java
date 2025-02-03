package com.hikmetsuicmez.eventverse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.hikmetsuicmez.eventverse.enums.PaymentStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Entity
@Getter
@Setter
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
    @Column(name = "location", nullable = false)
    private String location;
    @Column(name = "max_participants", nullable = false)
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

    @Column(name = "image_url")
    private String imageUrl;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL)
    private List<Participant> participants;

    @Column(name = "requires_approval", nullable = false)
    private Boolean requiresApproval = false;

    @Column(name = "payment_status")
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Column(name = "payment_deadline")
    private LocalDateTime paymentDeadline;

    @Column(name = "address")
    private String address;

}