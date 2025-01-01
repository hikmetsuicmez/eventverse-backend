package com.hikmetsuicmez.eventverse.repository;

import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.Participant;
import com.hikmetsuicmez.eventverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ParticipantRepository extends JpaRepository<Participant, UUID> {
    List<Participant> findByEvent(Event event);
    List<Participant> findByUser(User user);
    Optional<Participant> findByUserAndEvent(User user, Event event);
    boolean existsByUserAndEvent(User user, Event event);
    long countByEvent(Event event);
    boolean existsByEventIdAndUserId(UUID eventId, UUID userId);
    long countByEventId(UUID eventId);
    List<Participant> findByEventId(UUID id);
    Optional<Participant> findByEventIdAndId(UUID eventId, UUID participantId);
    boolean existsByEventAndUser(Event event, User user);
} 