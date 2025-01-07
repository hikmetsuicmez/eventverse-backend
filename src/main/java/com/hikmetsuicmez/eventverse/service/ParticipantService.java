package com.hikmetsuicmez.eventverse.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hikmetsuicmez.eventverse.dto.response.EventResponse;
import com.hikmetsuicmez.eventverse.dto.response.ParticipantResponse;
import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.Participant;
import com.hikmetsuicmez.eventverse.entity.User;
import com.hikmetsuicmez.eventverse.enums.ParticipantStatus;
import com.hikmetsuicmez.eventverse.exception.*;
import com.hikmetsuicmez.eventverse.repository.EventRepository;
import com.hikmetsuicmez.eventverse.repository.ParticipantRepository;
import com.hikmetsuicmez.eventverse.repository.UserRepository;
import com.hikmetsuicmez.eventverse.mapper.ParticipantMapper;
import com.hikmetsuicmez.eventverse.mapper.EventMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final ParticipantMapper participantMapper;
    private final EventMapper eventMapper;
    private final UserService userService;
    private final NotificationService notificationService;

    @Transactional
    public ParticipantResponse addParticipant(UUID eventId) {
        // 1. Etkinliği bul
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // 2. Mevcut kullanıcıyı al
        User currentUser = userService.getCurrentUser();

        // 3. Organizatör kontrolü
        if (event.getOrganizer().getId().equals(currentUser.getId())) {
            throw new OrganizerJoinException("Event organizer cannot join their own event");
        }

        // 4. Etkinlik tarihini kontrol et
        if (event.getDate().atStartOfDay().isBefore(LocalDateTime.now())) {
            throw new EventExpiredException("Event has already expired");
        }

        // 5. Kullanıcının zaten katılıp katılmadığını kontrol et
        if (participantRepository.existsByEventIdAndUserId(eventId, currentUser.getId())) {
            throw new AlreadyParticipatingException("You are already participating in this event");
        }

        // 6. Etkinlik kapasitesini kontrol et
        long currentParticipants = participantRepository.countByEventId(eventId);
        if (currentParticipants >= event.getMaxParticipants()) {
            throw new EventCapacityFullException("Event has reached its maximum capacity");
        }

        // 7. Yeni katılımcı oluştur
        Participant participant = Participant.builder()
                .event(event)
                .user(currentUser)
                .status(ParticipantStatus.PENDING)
                .registrationDate(LocalDateTime.now())
                .build();

        // 8. Katılımcıyı kaydet
        Participant savedParticipant = participantRepository.save(participant);

        try {
            // 9. Etkinlik sahibine bildirim gönder
            notificationService.createParticipationRequestNotification(savedParticipant);

            // 10. Katılımcıya bildirim gönder
            notificationService.createParticipationRequestConfirmationNotification(savedParticipant);
        } catch (Exception e) {
            // Bildirim gönderme hatası olsa bile katılım işlemi başarılı sayılır
            System.err.println("Bildirim gönderilirken hata oluştu: " + e.getMessage());
        }

        // 11. Response'u dön
        return participantMapper.toResponse(savedParticipant);
    }

    public List<ParticipantResponse> getParticipants(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        List<Participant> participants = participantRepository.findByEventId(event.getId());
        return participants
                .stream()
                .map(participantMapper::toResponse)
                .toList();
    }

    @Transactional
    public ParticipantResponse updateParticipantStatus(UUID eventId, UUID participantId, ParticipantStatus status) {
        // Status null kontrolü
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        // Etkinliği bul
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        // Katılımcıyı bul
        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found with id: " + participantId));

        // Event ID'sinin eşleşip eşleşmediğini kontrol et
        if (!participant.getEvent().getId().equals(event.getId())) {
            throw new ResourceNotFoundException("Participant not found for this event");
        }

        // İsteği yapan kullanıcının etkinliğin organizatörü olup olmadığını kontrol et
        UUID currentUserId = userService.getCurrentUserId();
        if (!event.getOrganizer().getId().equals(currentUserId)) {
            throw new UnauthorizedAccessException("Only event organizer can update participant status");
        }

        // Mevcut durumu kontrol et
        if (participant.getStatus() == status) {
            return participantMapper.toResponse(participant);
        }

        // Status güncelleme
        participant.setStatus(status);
        if (participant.getRegistrationDate() == null) {
            participant.setRegistrationDate(LocalDateTime.now());
        }
        
        // Önce katılımcıyı kaydet
        Participant savedParticipant = participantRepository.save(participant);
        
        try {
            // Bildirim oluştur
            notificationService.createParticipationStatusNotification(savedParticipant);
        } catch (Exception e) {
            // Bildirim hatası olsa bile işleme devam et
            System.err.println("Katılımcı durumu bildirimi gönderilirken hata: " + e.getMessage());
        }
        
        return participantMapper.toResponse(savedParticipant);
    }

    public List<EventResponse> getUserEvents() {
        User currentUser = userService.getCurrentUser();
        
        List<Participant> participants = participantRepository.findByUser(currentUser);
        
        return participants.stream()
            .filter(participant -> participant.getStatus() == ParticipantStatus.APPROVED)
            .map(participant -> eventMapper.toResponse(participant.getEvent()))
            .toList();
    }

    public List<EventResponse> getUserByIdEvents(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        List<Participant> participants = participantRepository.findByUser(user);
        return participants.stream()
                .map(participant -> eventMapper.toResponse(participant.getEvent()))
                .toList();
    }
}
