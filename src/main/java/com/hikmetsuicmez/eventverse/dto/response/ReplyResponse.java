package com.hikmetsuicmez.eventverse.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ReplyResponse {
    private UUID id;
    private String content;
    private LocalDateTime createdAt;
    private UserResponse user;
    private boolean isEventOwnerReply;
}
