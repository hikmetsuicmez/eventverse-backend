package com.hikmetsuicmez.eventverse.repository;

import com.hikmetsuicmez.eventverse.entity.Payment;
import com.hikmetsuicmez.eventverse.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    List<Payment> findByEventId(UUID eventId);
    List<Payment> findByUserId(UUID userId);
    Optional<Payment> findByEventIdAndUserId(UUID eventId, UUID userId);
    List<Payment> findByStatus(PaymentStatus status);
    Optional<Payment> findFirstByEventIdAndUserIdOrderByPaymentDateDesc(UUID eventId, UUID userId);
} 