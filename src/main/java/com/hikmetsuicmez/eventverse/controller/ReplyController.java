package com.hikmetsuicmez.eventverse.controller;

import com.hikmetsuicmez.eventverse.dto.request.ReplyRequest;
import com.hikmetsuicmez.eventverse.dto.response.ReplyResponse;
import com.hikmetsuicmez.eventverse.service.ReplyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/replies")
@RequiredArgsConstructor
@CrossOrigin
public class ReplyController {

    private final ReplyService replyService;

    @PostMapping
    public ResponseEntity<ReplyResponse> createReply(@PathVariable UUID commentId, @RequestBody ReplyRequest request) {
        return ResponseEntity.ok(replyService.createReply(commentId, request));
    }
} 