package com.hikmetsuicmez.eventverse.repository;

import com.hikmetsuicmez.eventverse.entity.Comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    @Query("SELECT c FROM Comment c WHERE c.event.id = :eventId ORDER BY c.createdAt DESC")
    List<Comment> findByEventId(@Param("eventId") UUID eventId);
}

