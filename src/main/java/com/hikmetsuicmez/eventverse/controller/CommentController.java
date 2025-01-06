package com.hikmetsuicmez.eventverse.controller;

import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hikmetsuicmez.eventverse.dto.request.ReplyRequest;
import com.hikmetsuicmez.eventverse.dto.response.ApiResponse;
import com.hikmetsuicmez.eventverse.dto.response.ReplyResponse;
import com.hikmetsuicmez.eventverse.service.ReplyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {

    private final ReplyService replyService;

    @PostMapping("/{commentId}/reply")
    public ApiResponse<ReplyResponse> createReply(@PathVariable UUID commentId, @RequestBody @Valid ReplyRequest request) {
        return ApiResponse.success(replyService.createReply(commentId, request), "Reply created successfully");
    }
}
