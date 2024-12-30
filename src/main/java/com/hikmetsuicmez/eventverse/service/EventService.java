package com.hikmetsuicmez.eventverse.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hikmetsuicmez.eventverse.dto.request.EventRequest;
import com.hikmetsuicmez.eventverse.dto.response.EventResponse;
import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.mapper.EventMapper;
import com.hikmetsuicmez.eventverse.repository.EventRepository;
import com.hikmetsuicmez.eventverse.entity.User;
import com.hikmetsuicmez.eventverse.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserService userService;

    public EventResponse createEvent(EventRequest request) {
        User currentUser = userService.getCurrentUser();
        
        Event event = eventMapper.toEntity(request);
        event.setOrganizer(currentUser);
        
        Event savedEvent = eventRepository.save(event);
        return eventMapper.toResponse(savedEvent);
    }

    public List<EventResponse> retrieveEvents(String category, String location, LocalDate date) {
        List<Event> events;

        if (category == null && location == null && date == null) {
            events = eventRepository.findAll();
        }
        else if (location == null && date == null) {
            events = eventRepository.findByCategory(category);
        }
        else if (category == null && date == null) {
            events = eventRepository.findByLocation(location);
        }
        else if (category == null && location == null) {
            events = eventRepository.findByDate(date);
        }
        else if (date == null) {
            events = eventRepository.findByCategoryAndLocation(category, location);
        }
        else if (location == null) {
            events = eventRepository.findByCategoryAndDate(category, date);
        }
        else if (category == null) {
            events = eventRepository.findByLocationAndDate(location, date);
        }
        else {
            events = eventRepository.findByCategoryAndLocationAndDate(category, location, date);
        }
        
        return events
                .stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    public EventResponse retrieveEvent(UUID id) {
        Event event = eventRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        return eventMapper.toResponse(event);
    }

}

