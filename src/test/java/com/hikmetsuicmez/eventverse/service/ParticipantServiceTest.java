package com.hikmetsuicmez.eventverse.service;

import com.hikmetsuicmez.eventverse.dto.response.EventResponse;
import com.hikmetsuicmez.eventverse.dto.response.ParticipantResponse;
import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.Participant;
import com.hikmetsuicmez.eventverse.entity.User;
import com.hikmetsuicmez.eventverse.enums.ParticipantStatus;
import com.hikmetsuicmez.eventverse.exception.*;
import com.hikmetsuicmez.eventverse.mapper.EventMapper;
import com.hikmetsuicmez.eventverse.mapper.ParticipantMapper;
import com.hikmetsuicmez.eventverse.repository.EventRepository;
import com.hikmetsuicmez.eventverse.repository.ParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipantServiceTest {

    @Mock
    private ParticipantRepository participantRepository;
    
    @Mock
    private EventRepository eventRepository;
    
    @Mock
    private ParticipantMapper participantMapper;
    
    @Mock
    private EventMapper eventMapper;
    
    @Mock
    private UserService userService;
    
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ParticipantService participantService;

    private User testUser;
    private Event testEvent;
    private Participant testParticipant;
    private ParticipantResponse testParticipantResponse;

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
                .date(LocalDate.now().plusDays(1))
                .maxParticipants(100)
                .organizer(testUser)
                .build();

        testParticipant = Participant.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .event(testEvent)
                .status(ParticipantStatus.PENDING)
                .registrationDate(LocalDateTime.now())
                .build();

        testParticipantResponse = ParticipantResponse.builder()
                .id(testParticipant.getId())
                .status(testParticipant.getStatus())
                .registrationDate(testParticipant.getRegistrationDate())
                .build();
    }

    @Test
    void addParticipant_ShouldCreateAndReturnParticipant() {
        // Arrange
        when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.of(testEvent));
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(participantRepository.existsByEventIdAndUserId(any(), any())).thenReturn(false);
        when(participantRepository.countByEventId(any())).thenReturn(0L);
        when(participantRepository.save(any(Participant.class))).thenReturn(testParticipant);
        when(participantMapper.toResponse(any(Participant.class))).thenReturn(testParticipantResponse);

        // Act
        ParticipantResponse result = participantService.addParticipant(testEvent.getId());

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ParticipantStatus.PENDING);
        verify(participantRepository).save(any(Participant.class));
        verify(notificationService).createParticipationRequestNotification(any(Participant.class));
    }

    @Test
    void addParticipant_WhenEventIsFull_ShouldThrowException() {
        // Arrange
        when(eventRepository.findById(any(UUID.class))).thenReturn(Optional.of(testEvent));
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(participantRepository.countByEventId(any())).thenReturn(100L);

        // Act & Assert
        assertThatThrownBy(() -> participantService.addParticipant(testEvent.getId()))
                .isInstanceOf(EventCapacityFullException.class);
    }

    @Test
    void updateParticipantStatus_ShouldUpdateAndReturnParticipant() {
        // Arrange
        when(eventRepository.findById(any())).thenReturn(Optional.of(testEvent));
        when(participantRepository.findById(any())).thenReturn(Optional.of(testParticipant));
        when(userService.getCurrentUserId()).thenReturn(testUser.getId());
        when(participantRepository.save(any())).thenReturn(testParticipant);
        when(participantMapper.toResponse(any())).thenReturn(testParticipantResponse);

        // Act
        ParticipantResponse result = participantService.updateParticipantStatus(
                testEvent.getId(), 
                testParticipant.getId(), 
                ParticipantStatus.APPROVED
        );

        // Assert
        assertThat(result).isNotNull();
        verify(participantRepository).save(any(Participant.class));
        verify(notificationService).createParticipationStatusNotification(any(Participant.class));
    }

    @Test
    void getUserEvents_ShouldReturnUserEvents() {
        // Arrange
        testParticipant.setStatus(ParticipantStatus.APPROVED);
        
        EventResponse testEventResponse = EventResponse.builder()
                .id(testEvent.getId())
                .title(testEvent.getTitle())
                .build();

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(participantRepository.findByUser(testUser)).thenReturn(List.of(testParticipant));
        when(eventMapper.toResponse(testEvent)).thenReturn(testEventResponse);

        // Act
        List<EventResponse> result = participantService.getUserEvents();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(testEvent.getId());
        verify(participantRepository).findByUser(testUser);
        verify(eventMapper).toResponse(testEvent);
    }
} 