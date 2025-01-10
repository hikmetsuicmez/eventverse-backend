package com.hikmetsuicmez.eventverse.dto.request;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventFilterRequest {

    @Size(max = 100, message = "Arama metni en fazla 100 karakter olmalıdır")
    private String searchText;

    @FutureOrPresent(message = "Tarih bugünden önce olamaz")
    private LocalDate startDate;

    @FutureOrPresent(message = "Tarih bugünden önce olamaz")
    private LocalDate endDate;

    private String location;
    private List<String> categories;

    @Min(value = 0, message = "Minimum fiyat 0 olmalıdır")
    private Double minPrice;

    @Max(value = 100000, message = "Maksimum fiyat 100000 olmalıdır")
    private Double maxPrice;

    @Min(value = 0, message = "Minimum yaş 0 olmalıdır")
    private Integer minAge;

    @Max(value = 100, message = "Maksimum yaş 100 olmalıdır")
    private Integer maxAge;

    private Boolean isPaid;
    private Boolean hasAgeLimit;

}
