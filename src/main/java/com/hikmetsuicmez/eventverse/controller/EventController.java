package com.hikmetsuicmez.eventverse.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.beans.factory.annotation.Value;

import com.hikmetsuicmez.eventverse.dto.request.CommentRequest;
import com.hikmetsuicmez.eventverse.dto.request.EventFilterRequest;
import com.hikmetsuicmez.eventverse.dto.request.EventRequest;
import com.hikmetsuicmez.eventverse.dto.response.ApiResponse;
import com.hikmetsuicmez.eventverse.dto.response.CommentResponse;
import com.hikmetsuicmez.eventverse.dto.response.EventFilterResponse;
import com.hikmetsuicmez.eventverse.dto.response.EventResponse;
import com.hikmetsuicmez.eventverse.dto.response.ParticipantResponse;
import com.hikmetsuicmez.eventverse.dto.response.EventLocationResponse;
import com.hikmetsuicmez.eventverse.enums.ParticipantStatus;
import com.hikmetsuicmez.eventverse.service.EventService;
import com.hikmetsuicmez.eventverse.service.ParticipantService;
import com.hikmetsuicmez.eventverse.service.CommentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final ParticipantService participantService;
    private final CommentService commentService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Event

    @PostMapping
    public ApiResponse<EventResponse> createEvent(@RequestBody @Valid EventRequest request) {
        return ApiResponse.success(eventService.createEvent(request), "Event created successfully");
    }

    @PostMapping("/filter")
    public ApiResponse<Page<EventFilterResponse>> filterEvents(
            @RequestBody @Valid EventFilterRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortOrder) {
        Page<EventFilterResponse> filteredEvents = eventService.filterEvents(request, page, size, sortBy, sortOrder);
        return ApiResponse.success(filteredEvents, "Events filtered successfully!");
    }

    @GetMapping
    public ApiResponse<List<EventResponse>> getEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        return ApiResponse.success(eventService.retrieveEvents(category, location, date),
                "Events retrieved successfully");
    }

    @GetMapping("/my-events")
    public ApiResponse<List<EventResponse>> getMyEvents() {
        return ApiResponse.success(participantService.getUserEvents(), "Katıldığınız etkinlikler başarıyla getirildi");
    }

    @GetMapping("/{userId}/events")
    public ApiResponse<List<EventResponse>> getUserEvents(@PathVariable UUID userId) {
        return ApiResponse.success(participantService.getUserByIdEvents(userId),
                "Kullanıcının etkinlikleri başarıyla getirildi");
    }

    @GetMapping("/{userId}/created-events")
    public ApiResponse<List<EventResponse>> getUserCreatedEvents(@PathVariable UUID userId) {
        return ApiResponse.success(eventService.getUserCreatedEvents(userId),
                "Kullanıcının oluşturduğu etkinlikler başarıyla getirildi");
    }

    @GetMapping("/my-created-events")
    public ApiResponse<List<EventResponse>> getMyCreatedEvents() {
        return ApiResponse.success(eventService.getCurrentUserEvents(),
                "Oluşturduğunuz etkinlikler başarıyla getirildi");
    }

    @GetMapping("/{eventId}")
    public ApiResponse<EventResponse> getEvent(@PathVariable UUID eventId) {
        return ApiResponse.success(eventService.retrieveEvent(eventId), "Event retrieved successfully");
    }

    // Participant

    @PostMapping("/{eventId}/participants")
    public ApiResponse<ParticipantResponse> addParticipant(@PathVariable UUID eventId) {
        return ApiResponse.success(participantService.addParticipant(eventId), "Participant added successfully");
    }

    @GetMapping("/{eventId}/participants")
    public ApiResponse<List<ParticipantResponse>> getParticipants(@PathVariable UUID eventId) {
        return ApiResponse.success(participantService.getParticipants(eventId), "Participants retrieved successfully");
    }

    @PatchMapping("/{eventId}/participants/{participantId}/status")
    public ResponseEntity<ParticipantResponse> updateParticipantStatus(
            @PathVariable UUID eventId,
            @PathVariable UUID participantId,
            @RequestParam ParticipantStatus status) {
        return ResponseEntity.ok(participantService.updateParticipantStatus(eventId, participantId, status));
    }

    // Location

    @GetMapping("/locations")
    public ApiResponse<List<EventLocationResponse>> getEventLocations() {
        return ApiResponse.success(
                eventService.getEventLocations(),
                "Event locations retrieved successfully");
    }

    @GetMapping("/{eventId}/location")
    public ApiResponse<EventLocationResponse> getEventLocation(@PathVariable UUID eventId) {
        return ApiResponse.success(
                eventService.getEventLocation(eventId),
                "Event location retrieved successfully");
    }

    // Comment

    @PostMapping("/{eventId}/comments")
    public ApiResponse<CommentResponse> createComment(@PathVariable UUID eventId, @RequestBody CommentRequest request) {
        return ApiResponse.success(commentService.createComment(eventId, request), "Comment created successfully");
    }

    @GetMapping("/{eventId}/comments")
    public ApiResponse<List<CommentResponse>> getComments(@PathVariable UUID eventId) {
        return ApiResponse.success(commentService.getCommentsByEventId(eventId), "Comments retrieved successfully");
    }

    @PostMapping("/{eventId}/image")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<String> uploadEventImage(
        @PathVariable UUID eventId,
        @RequestParam("image") MultipartFile image) {
        try {
            String imageUrl = eventService.uploadEventImage(eventId, image);
            return ApiResponse.success(imageUrl, "Resim başarıyla yüklendi");
        } catch (Exception e) {
            return ApiResponse.error("Resim yüklenirken bir hata oluştu: " + e.getMessage());
        }
    }

    @GetMapping("/images/{fileName}")
    public ResponseEntity<Resource> getEventImage(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(uploadDir + "/events/" + fileName);
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('USER')")
    public ApiResponse<EventResponse> updateEvent(
            @PathVariable UUID eventId,
            @RequestBody @Valid EventRequest request) {
        return ApiResponse.success(eventService.updateEvent(eventId, request), "Etkinlik başarıyla güncellendi");
    }

}