package com.hikmetsuicmez.eventverse.service;

import com.hikmetsuicmez.eventverse.dto.response.NotificationResponse;
import com.hikmetsuicmez.eventverse.entity.*;
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

    // Katılım isteği bildirimi
    public void createParticipationRequestNotification(Participant participant) {
        if (participant == null || participant.getUser() == null || 
            participant.getEvent() == null || participant.getEvent().getOrganizer() == null) {
            return;
        }

        try {
            Notification notification = Notification.builder()
                .recipient(participant.getEvent().getOrganizer())
                .event(participant.getEvent())
                .message(participant.getUser().getFirstName() + " " + 
                        participant.getUser().getLastName() + 
                        " kullanıcısı '" + participant.getEvent().getTitle() + 
                        "' etkinliğinize katılmak istiyor")
                .status(NotificationStatus.UNREAD)
                .timestamp(LocalDateTime.now())
                .build();
            
            notificationRepository.save(notification);
        } catch (Exception e) {
            System.err.println("Katılım isteği bildirimi oluşturulurken hata: " + e.getMessage());
        }
    }

    // Katılım isteği onay bildirimi
    public void createParticipationRequestConfirmationNotification(Participant participant) {
        if (participant == null || participant.getUser() == null || 
            participant.getEvent() == null) {
            return;
        }

        try {
            Notification notification = Notification.builder()
                .recipient(participant.getUser())
                .event(participant.getEvent())
                .message("'" + participant.getEvent().getTitle() + 
                        "' etkinliğine katılım talebiniz alındı. Organizatör onayı bekleniyor.")
                .status(NotificationStatus.UNREAD)
                .timestamp(LocalDateTime.now())
                .build();
            
            notificationRepository.save(notification);
        } catch (Exception e) {
            System.err.println("Katılım onay bildirimi oluşturulurken hata: " + e.getMessage());
        }
    }

    // Katılım durumu değişiklik bildirimi
    public void createParticipationStatusNotification(Participant participant) {
        if (participant == null || participant.getUser() == null || 
            participant.getEvent() == null) {
            return;
        }

        try {
            String statusMessage = participant.getStatus().toString().equals("APPROVED") ? 
                "kabul edildi" : "reddedildi";

            Notification notification = Notification.builder()
                .recipient(participant.getUser())
                .event(participant.getEvent())
                .message("'" + participant.getEvent().getTitle() + 
                        "' etkinliğine katılım talebiniz " + statusMessage)
                .status(NotificationStatus.UNREAD)
                .timestamp(LocalDateTime.now())
                .build();
            
            notificationRepository.save(notification);
        } catch (Exception e) {
            System.err.println("Katılım durumu bildirimi oluşturulurken hata: " + e.getMessage());
        }
    }

    // Etkinlik oluşturma bildirimi
    public void createEventCreationNotification(Event event) {
        if (event == null || event.getOrganizer() == null) {
            return;
        }

        try {
            Notification notification = Notification.builder()
                .recipient(event.getOrganizer())
                .event(event)
                .message("'" + event.getTitle() + "' etkinliğiniz başarıyla oluşturuldu. " +
                        "Katılımcı başvurularını beklemeye başlayabilirsiniz.")
                .status(NotificationStatus.UNREAD)
                .timestamp(LocalDateTime.now())
                .build();
            
            notificationRepository.save(notification);
        } catch (Exception e) {
            System.err.println("Etkinlik oluşturma bildirimi oluşturulurken hata: " + e.getMessage());
        }
    }

    // Favori ekleme bildirimi
    public void createFavoriteNotification(Favorite favorite) {
        if (favorite == null || favorite.getUser() == null || 
            favorite.getEvent() == null) {
            return;
        }

        try {
            Notification notification = Notification.builder()
                .recipient(favorite.getUser())
                .event(favorite.getEvent())
                .message("'" + favorite.getEvent().getTitle() + "' etkinliği favorilerinize eklendi")
                .status(NotificationStatus.UNREAD)
                .timestamp(LocalDateTime.now())
                .build();
                
            notificationRepository.save(notification);
        } catch (Exception e) {
            System.err.println("Favori ekleme bildirimi oluşturulurken hata: " + e.getMessage());
        }
    }

    // Favori silme bildirimi
    public void createFavoriteDeleteNotification(Favorite favorite) {
        if (favorite == null || favorite.getUser() == null || 
            favorite.getEvent() == null) {
            return;
        }

        try {
            Notification notification = Notification.builder()
                .recipient(favorite.getUser())
                .event(favorite.getEvent())
                .message("'" + favorite.getEvent().getTitle() + "' etkinliği favorilerinizden kaldırıldı")
                .status(NotificationStatus.UNREAD)
                .timestamp(LocalDateTime.now())
                .build();

            notificationRepository.save(notification);
        } catch (Exception e) {
            System.err.println("Favori silme bildirimi oluşturulurken hata: " + e.getMessage());
        }
    }

    // Yorum bildirimi
    public void createCommentNotification(Comment comment) {
        if (comment == null || comment.getUser() == null || 
            comment.getEvent() == null || comment.getEvent().getOrganizer() == null) {
            return;
        }

        // Eğer yorum yapan kişi etkinlik sahibiyse bildirim oluşturma
        if (comment.getUser().getId().equals(comment.getEvent().getOrganizer().getId())) {
            return;
        }

        try {
            Notification notification = Notification.builder()
                .recipient(comment.getEvent().getOrganizer())
                .event(comment.getEvent())
                .message(comment.getUser().getFirstName() + " " + 
                        comment.getUser().getLastName() + 
                        " kullanıcısı '" + comment.getEvent().getTitle() + 
                        "' etkinliğinize yorum yaptı: " + 
                        (comment.getContent().length() > 50 ? 
                            comment.getContent().substring(0, 50) + "..." : 
                            comment.getContent()))
                .status(NotificationStatus.UNREAD)
                .timestamp(LocalDateTime.now())
                .build();

            notificationRepository.save(notification);
        } catch (Exception e) {
            System.err.println("Yorum bildirimi oluşturulurken hata: " + e.getMessage());
        }
    }

    // Yorum cevabı bildirimi
    public void createReplyNotification(Reply reply) {
        if (reply == null || reply.getUser() == null || 
            reply.getComment() == null || reply.getComment().getUser() == null) {
            return;
        }

        try {
            Notification notification = Notification.builder()
                .recipient(reply.getComment().getUser())
                .event(reply.getComment().getEvent())
                .message(reply.getUser().getFirstName() + " " + 
                        reply.getUser().getLastName() + 
                        " kullanıcısı yorumunuza cevap verdi: " + 
                        (reply.getContent().length() > 50 ? 
                            reply.getContent().substring(0, 50) + "..." : 
                            reply.getContent()))
                .status(NotificationStatus.UNREAD)
                .timestamp(LocalDateTime.now())
                .build();

            notificationRepository.save(notification);
        } catch (Exception e) {
            System.err.println("Yorum cevabı bildirimi oluşturulurken hata: " + e.getMessage());
        }
    }

    // Bildirimi okundu olarak işaretle
    public NotificationResponse markAsRead(UUID notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(() -> new ResourceNotFoundException("Bildirim bulunamadı"));

        if (!notification.getRecipient().getId().equals(userService.getCurrentUserId())) {
            throw new UnauthorizedAccessException("Sadece kendi bildirimlerinizi okundu olarak işaretleyebilirsiniz");
        }

        notification.setStatus(NotificationStatus.READ);
        return notificationMapper.toResponse(notificationRepository.save(notification));
    }

    // Okunmamış bildirimleri getir
    public List<NotificationResponse> getUnreadNotifications() {
        List<Notification> notifications = notificationRepository
            .findByRecipientAndStatusOrderByTimestampDesc(
                userService.getCurrentUser(), 
                NotificationStatus.UNREAD
            );
        
        return notifications.stream()
            .map(notificationMapper::toResponse)
            .toList();
    }

    public void markAllAsRead() {
        List<Notification> notifications = notificationRepository.findByRecipientAndStatusOrderByTimestampDesc(
            userService.getCurrentUser(), 
            NotificationStatus.UNREAD
        );
        for (Notification notification : notifications) {
            notification.setStatus(NotificationStatus.READ);
        }
        notificationRepository.saveAll(notifications);
    }

    
} 