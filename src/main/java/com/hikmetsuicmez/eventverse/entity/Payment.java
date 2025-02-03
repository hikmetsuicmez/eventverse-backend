package com.hikmetsuicmez.eventverse.entity;

import com.hikmetsuicmez.eventverse.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private String transactionId;

    private LocalDateTime paymentDate;

    private LocalDateTime expirationDate;

    private String errorMessage;

    @Column(name = "installment_count")
    private Integer installmentCount;
} 