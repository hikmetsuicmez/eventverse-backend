package com.hikmetsuicmez.eventverse.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EventRequest {
    @NotBlank(message = "Başlık boş olamaz")
    @Size(min = 3, max = 100, message = "Başlık 3-100 karakter arasında olmalıdır")
    private String title;

    @NotBlank(message = "Açıklama boş olamaz")
    @Size(max = 500, message = "Açıklama en fazla 500 karakter olabilir")
    private String description;

    @NotNull(message = "Tarih boş olamaz")
    @Future(message = "Tarih gelecekte olmalıdır")
    private LocalDate date;

    @NotBlank(message = "Konum boş olamaz")
    @Size(max = 200, message = "Konum en fazla 200 karakter olabilir")
    private String location;

    @Min(value = 1, message = "Katılımcı sayısı en az 1 olmalıdır")
    @Max(value = 1000, message = "Katılımcı sayısı en fazla 1000 olabilir")
    private int maxParticipants;

    @NotBlank(message = "Kategori boş olamaz")
    private String category;
} 