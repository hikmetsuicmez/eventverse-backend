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
import com.hikmetsuicmez.eventverse.mapper.ParticipantMapper;
import com.hikmetsuicmez.eventverse.service.NotificationService;
import com.hikmetsuicmez.eventverse.mapper.EventMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final EventRepository eventRepository;
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

        // 3. Etkinlik tarihini kontrol et
        if (event.getDate().atStartOfDay().isBefore(LocalDateTime.now())) {
            throw new EventExpiredException("Event has already expired");
        }

        // 4. Kullanıcının zaten katılıp katılmadığını kontrol et
        if (participantRepository.existsByEventIdAndUserId(eventId, currentUser.getId())) {
            throw new AlreadyParticipatingException("You are already participating in this event");
        }

        // 5. Etkinlik kapasitesini kontrol et
        long currentParticipants = participantRepository.countByEventId(eventId);
        if (currentParticipants >= event.getMaxParticipants()) {
            throw new EventCapacityFullException("Event has reached its maximum capacity");
        }

        // 6. Yeni katılımcı oluştur
        Participant participant = Participant.builder()
                .event(event)
                .user(currentUser)
                .status(ParticipantStatus.PENDING)
                .registrationDate(LocalDateTime.now())
                .build();

        // 7. Katılımcıyı kaydet
        Participant savedParticipant = participantRepository.save(participant);

        // 8. Etkinlik sahibine bildirim gönder
        notificationService.createParticipationRequestNotification(savedParticipant);

        // 9. Katılımcıya bildirim gönder
        notificationService.createParticipationRequestConfirmationNotification(savedParticipant);

        // 10. Response'u dön
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

    public ParticipantResponse updateParticipantStatus(UUID eventId, UUID participantId, ParticipantStatus status) {
        System.out.println("Received status: " + status); // Debug için

        // Önce etkinliğin var olduğunu kontrol et
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

        // Status null kontrolü
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        // Enum değerini kontrol et
        try {
            ParticipantStatus.valueOf(status.toString());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status value: " + status);
        }

        // Status güncelleme
        participant.setStatus(status);
        
        // Eğer registration date null ise şu anki zamanı set et
        if (participant.getRegistrationDate() == null) {
            participant.setRegistrationDate(LocalDateTime.now());
        }
        
        Participant savedParticipant = participantRepository.save(participant);
        
        // Bildirim oluştur
        notificationService.createParticipationStatusNotification(savedParticipant);
        
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
}
