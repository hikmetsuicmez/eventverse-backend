package com.hikmetsuicmez.eventverse.mapper;

import com.hikmetsuicmez.eventverse.dto.request.ParticipantRequest;
import com.hikmetsuicmez.eventverse.dto.response.ParticipantResponse;
import com.hikmetsuicmez.eventverse.entity.Participant;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ParticipantMapper {
    Participant toEntity(ParticipantRequest request);
    ParticipantResponse toResponse(Participant participant);
} 