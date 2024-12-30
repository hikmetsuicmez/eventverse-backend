package com.hikmetsuicmez.eventverse.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMap {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "event_id")
    private Event event;

    private String mapURL;

    @Column(columnDefinition = "TEXT")
    private String interactiveElements; // JSON formatında saklanacak
}
