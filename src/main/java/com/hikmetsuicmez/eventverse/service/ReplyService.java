package com.hikmetsuicmez.eventverse.service;

import com.hikmetsuicmez.eventverse.dto.request.ReplyRequest;
import com.hikmetsuicmez.eventverse.dto.response.ReplyResponse;
import com.hikmetsuicmez.eventverse.entity.Comment;
import com.hikmetsuicmez.eventverse.entity.Reply;
import com.hikmetsuicmez.eventverse.entity.User;
import com.hikmetsuicmez.eventverse.exception.ResourceNotFoundException;
import com.hikmetsuicmez.eventverse.mapper.CommentMapper;
import com.hikmetsuicmez.eventverse.repository.CommentRepository;
import com.hikmetsuicmez.eventverse.repository.ReplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReplyService {

    private final ReplyRepository replyRepository;
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final CommentMapper commentMapper;
    private final NotificationService notificationService;


    public ReplyResponse createReply(UUID commentId, ReplyRequest request) {
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("İçerik boş olamaz");
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found"));

        User currentUser = userService.getCurrentUser();

        boolean isOwner = comment.getEvent().getOrganizer().getId().equals(currentUser.getId());

        Reply reply = Reply.builder()
                .content(request.getContent().trim())
                .comment(comment)
                .user(currentUser)
                .createdAt(LocalDateTime.now())
                .eventOwnerReply(isOwner)
                .build();

        Reply savedReply = replyRepository.save(reply);

        if (!currentUser.getId().equals(comment.getUser().getId())) {
            notificationService.createReplyNotification(savedReply);
        }

        return commentMapper.toReplyResponse(savedReply);
    }
}
