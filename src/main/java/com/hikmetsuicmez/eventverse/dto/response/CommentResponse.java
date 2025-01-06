package com.hikmetsuicmez.eventverse.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class CommentResponse {
    private UUID id;
    private String content;
    private LocalDateTime createdAt;
    private UserResponse user;
    private List<ReplyResponse> replies;
}
