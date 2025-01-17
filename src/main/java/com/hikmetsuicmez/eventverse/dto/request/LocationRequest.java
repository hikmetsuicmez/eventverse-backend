package com.hikmetsuicmez.eventverse.dto.request;

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

    private String city;
    private String country;
}