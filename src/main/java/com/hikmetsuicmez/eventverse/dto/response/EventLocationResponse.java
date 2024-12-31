package com.hikmetsuicmez.eventverse.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EventLocationResponse {
    private String title;
    private String address;
    private Double latitude;
    private Double longitude;
    private String city;
    private String country;
} 