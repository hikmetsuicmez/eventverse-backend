package com.hikmetsuicmez.eventverse.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerResponse {
    private String firstName;
    private String lastName;
    private String email;
} 