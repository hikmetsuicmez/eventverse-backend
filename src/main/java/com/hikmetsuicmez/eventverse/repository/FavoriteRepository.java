package com.hikmetsuicmez.eventverse.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.Favorite;
import com.hikmetsuicmez.eventverse.entity.User;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    @Query("SELECT f FROM Favorite f WHERE f.user.id = :userId")
    List<Favorite> findByUserId(@Param("userId") UUID userId);

    boolean existsByUserAndEvent(User user, Event event);

    Optional<Favorite> findByUserAndEvent(User user, Event event);
}
