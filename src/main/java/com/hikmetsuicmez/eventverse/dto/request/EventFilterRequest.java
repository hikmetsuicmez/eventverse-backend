package com.hikmetsuicmez.eventverse.dto.request;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventFilterRequest {

    @Size(max = 100, message = "Arama metni en fazla 100 karakter olmalıdır")
    private String searchText;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
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
