package com.hikmetsuicmez.eventverse.repository;

import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByOrganizer(User organizer);
    boolean existsByTitleAndOrganizer(String title, User organizer);
    
    List<Event> findByCategory(String category);
    List<Event> findByLocation(String location);
    List<Event> findByDate(LocalDate date);
    
    List<Event> findByCategoryAndLocation(String category, String location);
    List<Event> findByCategoryAndDate(String category, LocalDate date);
    List<Event> findByLocationAndDate(String location, LocalDate date);
    List<Event> findByCategoryAndLocationAndDate(String category, String location, LocalDate date);
} 