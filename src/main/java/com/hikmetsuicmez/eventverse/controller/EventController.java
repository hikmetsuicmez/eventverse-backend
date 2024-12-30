package com.hikmetsuicmez.eventverse.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hikmetsuicmez.eventverse.dto.request.EventRequest;
import com.hikmetsuicmez.eventverse.dto.response.ApiResponse;
import com.hikmetsuicmez.eventverse.dto.response.EventResponse;
import com.hikmetsuicmez.eventverse.service.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ApiResponse<EventResponse> createEvent(@RequestBody @Valid EventRequest request) {
        return ApiResponse.success(eventService.createEvent(request), "Event created successfully");
    }

    @GetMapping
    public ApiResponse<List<EventResponse>> getEvents(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        return ApiResponse.success(eventService.retrieveEvents(category, location, date), 
            "Events retrieved successfully");
    }

    @GetMapping("/{id}")
    public ApiResponse<EventResponse> getEvent(@PathVariable UUID id) {
        return ApiResponse.success(eventService.retrieveEvent(id), "Event retrieved successfully");
    }
    
}