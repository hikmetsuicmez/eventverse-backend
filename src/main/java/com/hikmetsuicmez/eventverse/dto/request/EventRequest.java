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
    private String location;

    @NotBlank(message = "Adres detayı boş olamaz")
    private String address;

    @NotNull(message = "Konum bilgisi eksik")
    private LocationRequest coordinates;

    @Min(value = 1, message = "Katılımcı sayısı en az 1 olmalıdır")
    @Max(value = 1000, message = "Katılımcı sayısı en fazla 1000 olabilir")
    private int maxParticipants;

    @NotBlank(message = "Kategori boş olamaz")
    private String category;

    @Pattern(regexp = "^(https?://)?[\\w-]+(\\.[\\w-]+)+[/#?]?.*\\.(jpg|jpeg|png|gif)$", message = "Geçerli bir resim URL'si giriniz")
    private String eventImage;

    private boolean hasAgeLimit;

    @Min(value = 0, message = "Yaş sınırı 0'dan küçük olamaz")
    @Max(value = 100, message = "Yaş sınırı 100'den büyük olamaz")
    private Integer ageLimit;

    private boolean isPaid;

    @DecimalMin(value = "0.0", message = "Ücret 0'dan küçük olamaz")
    @DecimalMax(value = "100000.0", message = "Ücret 100.000 TL'den fazla olamaz")
    private Double price;

    @AssertTrue(message = "Yaş sınırı varsa, yaş limiti belirtilmelidir")
    private boolean isAgeLimitValid() {
        if (hasAgeLimit) {
            return ageLimit != null;
        }
        return true;
    }

    @AssertTrue(message = "Etkinlik ücretliyse, ücret belirtilmelidir")
    private boolean isPriceValid() {
        if (isPaid) {
            return price != null && price > 0;
        }
        return true;
    }
}