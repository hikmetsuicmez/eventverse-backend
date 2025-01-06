package com.hikmetsuicmez.eventverse.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReplyRequest {
    @NotBlank(message = "İçerik boş olamaz")
    private String content;
}
