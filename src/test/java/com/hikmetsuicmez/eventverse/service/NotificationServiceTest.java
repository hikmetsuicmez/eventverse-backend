package com.hikmetsuicmez.eventverse.service;

import com.hikmetsuicmez.eventverse.dto.response.NotificationResponse;
import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.Notification;
import com.hikmetsuicmez.eventverse.entity.Participant;
import com.hikmetsuicmez.eventverse.entity.User;
import com.hikmetsuicmez.eventverse.enums.NotificationStatus;
import com.hikmetsuicmez.eventverse.enums.ParticipantStatus;
import com.hikmetsuicmez.eventverse.exception.UnauthorizedAccessException;
import com.hikmetsuicmez.eventverse.mapper.NotificationMapper;
import com.hikmetsuicmez.eventverse.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserService userService;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;
    private Event testEvent;
    private Notification testNotification;
    private NotificationResponse testNotificationResponse;
    private Participant testParticipant;

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
                .organizer(testUser)
                .build();

        testNotification = Notification.builder()
                .id(UUID.randomUUID())
                .recipient(testUser)
                .event(testEvent)
                .message("Test notification")
                .status(NotificationStatus.UNREAD)
                .timestamp(LocalDateTime.now())
                .build();

        testNotificationResponse = NotificationResponse.builder()
                .id(testNotification.getId())
                .message(testNotification.getMessage())
                .status(testNotification.getStatus())
                .timestamp(testNotification.getTimestamp())
                .eventTitle(testEvent.getTitle())
                .build();

        testParticipant = Participant.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .event(testEvent)
                .status(ParticipantStatus.PENDING)
                .build();
    }

    @Test
    void getUserNotifications_ShouldReturnUserNotifications() {
        // Arrange
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(notificationRepository.findByRecipientOrderByTimestampDesc(testUser))
                .thenReturn(List.of(testNotification));
        when(notificationMapper.toResponse(testNotification)).thenReturn(testNotificationResponse);

        // Act
        List<NotificationResponse> result = notificationService.getUserNotifications();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMessage()).isEqualTo(testNotification.getMessage());
        verify(notificationRepository).findByRecipientOrderByTimestampDesc(testUser);
    }

    @Test
    void markAsRead_ShouldUpdateNotificationStatus() {
        // Arrange
        when(notificationRepository.findById(testNotification.getId()))
                .thenReturn(Optional.of(testNotification));
        when(userService.getCurrentUserId()).thenReturn(testUser.getId());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification savedNotification = invocation.getArgument(0);
            assertThat(savedNotification.getStatus()).isEqualTo(NotificationStatus.READ);
            return savedNotification;
        });
        when(notificationMapper.toResponse(any(Notification.class))).thenReturn(testNotificationResponse);

        // Act
        NotificationResponse result = notificationService.markAsRead(testNotification.getId());

        // Assert
        assertThat(result).isNotNull();
        verify(notificationRepository).save(argThat(notification -> 
            notification.getStatus() == NotificationStatus.READ
        ));
    }

    @Test
    void markAsRead_WithUnauthorizedUser_ShouldThrowException() {
        // Arrange
        UUID differentUserId = UUID.randomUUID();
        when(notificationRepository.findById(any())).thenReturn(Optional.of(testNotification));
        when(userService.getCurrentUserId()).thenReturn(differentUserId);

        // Act & Assert
        assertThatThrownBy(() -> notificationService.markAsRead(testNotification.getId()))
                .isInstanceOf(UnauthorizedAccessException.class);
    }

    @Test
    void createParticipationStatusNotification_ShouldCreateNotification() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.createParticipationStatusNotification(testParticipant);

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void createEventCreationNotification_ShouldCreateNotification() {
        // Arrange
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);

        // Act
        notificationService.createEventCreationNotification(testEvent);

        // Assert
        verify(notificationRepository).save(any(Notification.class));
    }
} 