package com.hikmetsuicmez.eventverse.service;

import com.hikmetsuicmez.eventverse.dto.response.NotificationResponse;
import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.Notification;
import com.hikmetsuicmez.eventverse.entity.Participant;
import com.hikmetsuicmez.eventverse.enums.NotificationStatus;
import com.hikmetsuicmez.eventverse.exception.ResourceNotFoundException;
import com.hikmetsuicmez.eventverse.exception.UnauthorizedAccessException;
import com.hikmetsuicmez.eventverse.mapper.NotificationMapper;
import com.hikmetsuicmez.eventverse.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final NotificationMapper notificationMapper;

    public List<NotificationResponse> getUserNotifications() {
        List<Notification> notifications = notificationRepository
            .findByRecipientOrderByTimestampDesc(userService.getCurrentUser());
        
        return notifications.stream()
            .map(notificationMapper::toResponse)
            .toList();
    }

    // Katılım durumu değiştiğinde bildirim oluştur
    public void createParticipationStatusNotification(Participant participant) {
        Notification notification = Notification.builder()
            .recipient(participant.getUser())
            .event(participant.getEvent())
            .message("Katılım talebiniz " + participant.getStatus().toString().toLowerCase() + " edildi: " 
                    + participant.getEvent().getTitle())
            .status(NotificationStatus.UNREAD)
            .timestamp(LocalDateTime.now())
            .build();
        
        notificationRepository.save(notification);
    }

    public NotificationResponse markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        // Bildirimin sahibi olup olmadığını kontrol et
        if (!notification.getRecipient().getId().equals(userService.getCurrentUserId())) {
            throw new UnauthorizedAccessException("You can only mark your own notifications as read");
        }

        notification.setStatus(NotificationStatus.READ);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    public List<NotificationResponse> getUnreadNotifications() {
        List<Notification> notifications = notificationRepository
            .findByRecipientAndStatusOrderByTimestampDesc(userService.getCurrentUser(), NotificationStatus.UNREAD);
        
        return notifications.stream()
            .map(notificationMapper::toResponse)
            .toList();
    }

    // Yeni metod ekleyelim
    public void createParticipationRequestNotification(Participant participant) {
        Notification notification = Notification.builder()
            .recipient(participant.getEvent().getOrganizer()) // Etkinlik sahibine bildirim
            .event(participant.getEvent())
            .message(participant.getUser().getFirstName() + " " + 
                    participant.getUser().getLastName() + 
                    " etkinliğinize katılmak istiyor: " + 
                    participant.getEvent().getTitle())
            .status(NotificationStatus.UNREAD)
            .timestamp(LocalDateTime.now())
            .build();
        
        notificationRepository.save(notification);
    }

    public void createParticipationRequestConfirmationNotification(Participant participant) {
        Notification notification = Notification.builder()
            .recipient(participant.getUser()) // Katılımcıya bildirim
            .event(participant.getEvent())
            .message(participant.getEvent().getTitle() + " etkinliğine katılım talebiniz alındı. " + 
                    "Organizatör onayı bekleniyor.")
            .status(NotificationStatus.UNREAD)
            .timestamp(LocalDateTime.now())
            .build();
        
        notificationRepository.save(notification);
    }

    public void createEventCreationNotification(Event event) {
        Notification notification = Notification.builder()
            .recipient(event.getOrganizer())
            .event(event)
            .message("'" + event.getTitle() + "' etkinliğiniz başarıyla oluşturuldu. " +
                    "Katılımcı başvurularını beklemeye başlayabilirsiniz.")
            .status(NotificationStatus.UNREAD)
            .timestamp(LocalDateTime.now())
            .build();
        
        notificationRepository.save(notification);
    }
} 