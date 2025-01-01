package com.hikmetsuicmez.eventverse.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationRequest {

    @NotNull(message = "Enlem bilgisi gerekli")
    private Double latitude;

    @NotNull(message = "Boylam bilgisi gerekli")
    private Double longitude;

    @NotBlank(message = "Şehir bilgisi gerekli")
    private String city;

    @NotBlank(message = "Ülke bilgisi gerekli")
    private String country;
}