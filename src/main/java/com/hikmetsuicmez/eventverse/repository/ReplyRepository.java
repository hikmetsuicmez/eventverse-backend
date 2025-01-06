package com.hikmetsuicmez.eventverse.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hikmetsuicmez.eventverse.entity.Reply;

public interface ReplyRepository extends JpaRepository<Reply, UUID> {

    @Query("SELECT r FROM Reply r WHERE r.comment.id = :commentId ORDER BY r.createdAt ASC")
    List<Reply> findByCommentId(@Param("commentId") UUID commentId);

}
