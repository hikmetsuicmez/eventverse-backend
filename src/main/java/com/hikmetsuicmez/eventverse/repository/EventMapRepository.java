package com.hikmetsuicmez.eventverse.repository;

import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.EventMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventMapRepository extends JpaRepository<EventMap, UUID> {
    Optional<EventMap> findByEvent(Event event);
    boolean existsByEvent(Event event);
} 