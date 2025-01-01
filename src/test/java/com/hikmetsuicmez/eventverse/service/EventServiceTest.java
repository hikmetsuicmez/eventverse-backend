package com.hikmetsuicmez.eventverse.service;

import com.hikmetsuicmez.eventverse.dto.request.EventRequest;
import com.hikmetsuicmez.eventverse.dto.response.EventResponse;
import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.User;
import com.hikmetsuicmez.eventverse.mapper.EventMapper;
import com.hikmetsuicmez.eventverse.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private UserService userService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private EventService eventService;

    private User testUser;
    private Event testEvent;
    private EventRequest testEventRequest;
    private EventResponse testEventResponse;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .firstName("Test")
                .lastName("User")
                .build();

        testEvent = Event.builder()
                .id(UUID.randomUUID())
                .title("Test Event")
                .description("Test Description")
                .date(LocalDate.now().plusDays(1))
                .location("Test Location")
                .maxParticipants(100)
                .category("Test Category")
                .organizer(testUser)
                .build();

        testEventRequest = EventRequest.builder()
                .title("Test Event")
                .description("Test Description")
                .date(LocalDate.now().plusDays(1))
                .location("Test Location")
                .address("Test Address")
                .maxParticipants(100)
                .category("Test Category")
                .build();

        testEventResponse = EventResponse.builder()
                .id(testEvent.getId())
                .title(testEvent.getTitle())
                .description(testEvent.getDescription())
                .date(testEvent.getDate())
                .location(testEvent.getLocation())
                .maxParticipants(testEvent.getMaxParticipants())
                .category(testEvent.getCategory())
                .build();
    }

    @Test
    void createEvent_ShouldCreateAndReturnEvent() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(eventMapper.toEntity(any(EventRequest.class))).thenReturn(testEvent);
        when(eventRepository.save(any(Event.class))).thenReturn(testEvent);
        when(eventMapper.toResponse(any(Event.class))).thenReturn(testEventResponse);

        // Act
        EventResponse result = eventService.createEvent(testEventRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo(testEventRequest.getTitle());
        assertThat(result.getDescription()).isEqualTo(testEventRequest.getDescription());
        
        verify(userService).getCurrentUser();
        verify(eventRepository).save(any(Event.class));
        verify(notificationService).createEventCreationNotification(any(Event.class));
    }

    @Test
    void retrieveEvents_WithNoFilters_ShouldReturnAllEvents() {
        // Arrange
        when(eventRepository.findAll()).thenReturn(List.of(testEvent));
        when(eventMapper.toResponse(any(Event.class))).thenReturn(testEventResponse);

        // Act
        List<EventResponse> result = eventService.retrieveEvents(null, null, null);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo(testEvent.getTitle());
        
        verify(eventRepository).findAll();
    }

    @Test
    void retrieveEvent_WithValidId_ShouldReturnEvent() {
        // Arrange
        UUID eventId = testEvent.getId();
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(testEvent));
        when(eventMapper.toResponse(testEvent)).thenReturn(testEventResponse);

        // Act
        EventResponse result = eventService.retrieveEvent(eventId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(eventId);
        assertThat(result.getTitle()).isEqualTo(testEvent.getTitle());
        
        verify(eventRepository).findById(eventId);
    }
} 