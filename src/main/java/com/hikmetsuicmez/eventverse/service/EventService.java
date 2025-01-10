package com.hikmetsuicmez.eventverse.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hikmetsuicmez.eventverse.dto.request.EventFilterRequest;
import com.hikmetsuicmez.eventverse.dto.request.EventRequest;
import com.hikmetsuicmez.eventverse.dto.response.EventResponse;
import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.mapper.EventMapper;
import com.hikmetsuicmez.eventverse.repository.EventRepository;
import com.hikmetsuicmez.eventverse.repository.UserRepository;

import com.hikmetsuicmez.eventverse.entity.User;
import com.hikmetsuicmez.eventverse.exception.ResourceNotFoundException;
import com.hikmetsuicmez.eventverse.dto.response.EventFilterResponse;
import com.hikmetsuicmez.eventverse.dto.response.EventLocationResponse;
import com.hikmetsuicmez.eventverse.mapper.EventLocationMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final EventLocationMapper eventLocationMapper;

    @Transactional
    public EventResponse createEvent(EventRequest request) {
        User currentUser = userService.getCurrentUser();

        Event event = eventMapper.toEntity(request);
        event.setOrganizer(currentUser);

        Event savedEvent = eventRepository.save(event);

        notificationService.createEventCreationNotification(savedEvent);

        return eventMapper.toResponse(savedEvent);
    }

    public Page<EventFilterResponse> filterEvents(EventFilterRequest request, int page, int size, String sortBy,
            String sortOrder) {
        // Sıralama yönünü belirle
        Sort.Direction direction = Sort.Direction.ASC;
        if (sortOrder != null && !sortOrder.isEmpty()) {
            try {
                direction = Sort.Direction.fromString(sortOrder.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Hatalı sıralama yönü durumunda varsayılan olarak ASC kullan
            }
        }

        // Sıralama alanını kontrol et ve varsayılan değeri ayarla
        if (sortBy == null || sortBy.isEmpty()) {
            sortBy = "date";
        }

        // Pageable nesnesini oluştur
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        // Filtreleme işlemini gerçekleştir
        Page<Event> eventPage = eventRepository.filterEvents(
                request.getSearchText(),
                request.getStartDate(),
                request.getEndDate(),
                request.getCategories(),
                request.getLocation(),
                request.getMinPrice(),
                request.getMaxPrice(),
                request.getMinAge(),
                request.getMaxAge(),
                request.getIsPaid(),
                request.getHasAgeLimit(),
                pageable);

        // Event'leri EventFilterResponse'a dönüştür
        return eventPage.map(event -> new EventFilterResponse(event));
    }

    public List<EventResponse> retrieveEvents(String category, String location, LocalDate date) {
        List<Event> events;

        if (category == null && location == null && date == null) {
            events = eventRepository.findAll();
        } else if (location == null && date == null) {
            events = eventRepository.findByCategory(category);
        } else if (category == null && date == null) {
            events = eventRepository.findByLocation(location);
        } else if (category == null && location == null) {
            events = eventRepository.findByDate(date);
        } else if (date == null) {
            events = eventRepository.findByCategoryAndLocation(category, location);
        } else if (location == null) {
            events = eventRepository.findByCategoryAndDate(category, date);
        } else if (category == null) {
            events = eventRepository.findByLocationAndDate(location, date);
        } else {
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

    public List<EventLocationResponse> getEventLocations() {
        return eventRepository.findAll().stream()
                .map(eventLocationMapper::toLocationResponse)
                .toList();
    }

    public EventLocationResponse getEventLocation(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        return eventLocationMapper.toLocationResponse(event);
    }

    public List<EventResponse> getCurrentUserEvents() {
        User currentUser = userService.getCurrentUser();
        List<Event> events = eventRepository.findByOrganizer(currentUser);
        return events.stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    public EventResponse getEventById(UUID id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));
        return eventMapper.toResponse(event);
    }

    public List<EventResponse> getUserCreatedEvents(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<Event> events = eventRepository.findByOrganizer(user);
        return events.stream()
                .map(eventMapper::toResponse)
                .toList();
    }

}
