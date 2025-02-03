package com.hikmetsuicmez.eventverse.repository;

import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID>, EventRepositoryCustom {
    List<Event> findByOrganizer(User organizer);

    boolean existsByTitleAndOrganizer(String title, User organizer);

    List<Event> findByCategory(String category);

    List<Event> findByLocation(String location);

    List<Event> findByDate(LocalDate date);

    List<Event> findByCategoryAndLocation(String category, String location);

    List<Event> findByCategoryAndDate(String category, LocalDate date);

    List<Event> findByLocationAndDate(String location, LocalDate date);

    List<Event> findByCategoryAndLocationAndDate(String category, String location, LocalDate date);

    List<Event> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String title, String description);

    @Query("SELECT e FROM Event e WHERE " +
           "(:category IS NULL OR e.category = :category) AND " +
           "(:minPrice IS NULL OR e.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR e.price <= :maxPrice) AND " +
           "(:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
           "(:startDate IS NULL OR e.date >= :startDate) AND " +
           "(:endDate IS NULL OR e.date <= :endDate) AND " +
           "(:isPaid IS NULL OR e.isPaid = :isPaid) AND " +
           "(:hasAgeLimit IS NULL OR e.hasAgeLimit = :hasAgeLimit) AND " +
           "(:requiresApproval IS NULL OR e.requiresApproval = :requiresApproval)")
    Page<Event> findEventsWithFilters(
            @Param("category") String category,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("location") String location,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("isPaid") Boolean isPaid,
            @Param("hasAgeLimit") Boolean hasAgeLimit,
            @Param("requiresApproval") Boolean requiresApproval,
            Pageable pageable
    );
}