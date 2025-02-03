package com.hikmetsuicmez.eventverse.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import com.hikmetsuicmez.eventverse.dto.response.EventResponse;
import com.hikmetsuicmez.eventverse.dto.response.ParticipantResponse;
import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.Participant;
import com.hikmetsuicmez.eventverse.entity.User;
import com.hikmetsuicmez.eventverse.enums.ParticipantStatus;
import com.hikmetsuicmez.eventverse.event.PaymentRequiredEvent;
import com.hikmetsuicmez.eventverse.event.ParticipantStatusUpdateEvent;
import com.hikmetsuicmez.eventverse.exception.*;
import com.hikmetsuicmez.eventverse.repository.EventRepository;
import com.hikmetsuicmez.eventverse.repository.ParticipantRepository;
import com.hikmetsuicmez.eventverse.repository.UserRepository;
import com.hikmetsuicmez.eventverse.mapper.ParticipantMapper;
import com.hikmetsuicmez.eventverse.mapper.EventMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;

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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ParticipantResponse addParticipant(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        User currentUser = userService.getCurrentUser();

        // Temel kontroller
        validateParticipation(event, currentUser);

        // Katılımcı durumunu belirle
        ParticipantStatus initialStatus;
        if (event.getRequiresApproval()) {
            initialStatus = ParticipantStatus.PENDING; // Organizatör onayı bekleniyor
        } else if (event.isPaid()) {
            initialStatus = ParticipantStatus.PAYMENT_PENDING; // Direkt ödeme bekleniyor
        } else {
            initialStatus = ParticipantStatus.APPROVED; // Ücretsiz ve onaysız etkinlik
        }

        Participant participant = Participant.builder()
                .event(event)
                .user(currentUser)
                .status(initialStatus)
                .registrationDate(LocalDateTime.now())
                .build();

        Participant savedParticipant = participantRepository.save(participant);

        try {
            if (event.getRequiresApproval()) {
                // Organizatör onayı gerekiyorsa, onay bildirimleri gönder
                notificationService.createParticipationRequestNotification(savedParticipant);
                notificationService.createParticipationRequestConfirmationNotification(savedParticipant);
            } else if (event.isPaid()) {
                // Organizatör onayı gerekmiyorsa ve ücretliyse, direkt ödeme sürecini başlat
                notificationService.createPaymentTimeStartedNotification(savedParticipant);
                eventPublisher.publishEvent(new PaymentRequiredEvent(event.getId(), currentUser.getId()));
            }
        } catch (Exception e) {
            System.err.println("Bildirim gönderilirken hata oluştu: " + e.getMessage());
        }

        return participantMapper.toResponse(savedParticipant);
    }

    @Transactional
    public ParticipantResponse updateParticipantStatus(UUID eventId, UUID participantId, ParticipantStatus status) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        Participant participant = participantRepository.findById(participantId)
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found with id: " + participantId));

        if (!participant.getEvent().getId().equals(event.getId())) {
            throw new ResourceNotFoundException("Participant not found for this event");
        }

        // Eğer etkinlik ücretliyse ve katılımcı onaylanıyorsa, ödeme durumuna geç
        if (status == ParticipantStatus.APPROVED && event.isPaid()) {
            status = ParticipantStatus.PAYMENT_PENDING;
            notificationService.createPaymentTimeStartedNotification(participant);
            eventPublisher.publishEvent(new PaymentRequiredEvent(event.getId(), participant.getUser().getId()));
        }

        participant.setStatus(status);
        Participant updatedParticipant = participantRepository.save(participant);

        // Katılımcıya bildirim gönder
        notificationService.createParticipationStatusNotification(updatedParticipant);

        return participantMapper.toResponse(updatedParticipant);
    }

    private void validateParticipation(Event event, User user) {
        if (event.getOrganizer().getId().equals(user.getId())) {
            throw new OrganizerJoinException("Event organizer cannot join their own event");
        }

        // Tarih ve saat kontrolü
        LocalDateTime eventDateTime = event.getDate().atTime(
            Integer.parseInt(event.getEventTime().split(":")[0]),
            Integer.parseInt(event.getEventTime().split(":")[1])
        );
        
        if (eventDateTime.isBefore(LocalDateTime.now())) {
            throw new EventExpiredException("Etkinlik tarihi geçmiş");
        }

        if (participantRepository.existsByEventIdAndUserId(event.getId(), user.getId())) {
            throw new AlreadyParticipatingException("You are already participating in this event");
        }

        long currentParticipants = participantRepository.countByEventId(event.getId());
        if (currentParticipants >= event.getMaxParticipants()) {
            throw new EventCapacityFullException("Event has reached its maximum capacity");
        }
    }

    @EventListener
    public void handleParticipantStatusUpdateEvent(ParticipantStatusUpdateEvent event) {
        Participant participant = participantRepository.findByEventIdAndUserId(event.getEventId(), event.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));
        
        participant.setStatus(event.getStatus());
        participantRepository.save(participant);
    }

    public List<ParticipantResponse> getParticipants(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        List<Participant> participants = participantRepository.findByEventId(event.getId());
        return participants.stream()
                .map(participantMapper::toResponse)
                .toList();
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
