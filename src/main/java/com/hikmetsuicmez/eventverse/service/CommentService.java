package com.hikmetsuicmez.eventverse.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hikmetsuicmez.eventverse.dto.request.CommentRequest;
import com.hikmetsuicmez.eventverse.dto.response.CommentResponse;
import com.hikmetsuicmez.eventverse.entity.Comment;
import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.User;
import com.hikmetsuicmez.eventverse.exception.ResourceNotFoundException;
import com.hikmetsuicmez.eventverse.mapper.CommentMapper;
import com.hikmetsuicmez.eventverse.repository.CommentRepository;
import com.hikmetsuicmez.eventverse.repository.EventRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserService userService;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;  

    @Transactional
    public CommentResponse createComment(UUID eventId, CommentRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        User currentUser = userService.getCurrentUser();

        Comment comment = Comment.builder()
                .content(request.getContent())
                .event(event)
                .user(currentUser)
                .createdAt(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);

        try {
            // Yorum bildirimi gönder
            notificationService.createCommentNotification(savedComment);
        } catch (Exception e) {
            // Bildirim gönderme hatası olsa bile yorum kaydedilmiş olacak
            System.err.println("Yorum bildirimi gönderilirken hata: " + e.getMessage());
        }

        return commentMapper.toCommentResponse(savedComment);
    }


    public List<CommentResponse> getCommentsByEventId(UUID eventId) {
        List<Comment> comments = commentRepository.findByEventId(eventId);
        return commentMapper.toCommentResponseList(comments);
    }

   
}
