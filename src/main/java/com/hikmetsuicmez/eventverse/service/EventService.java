package com.hikmetsuicmez.eventverse.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.web.multipart.MultipartFile;

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
import com.hikmetsuicmez.eventverse.exception.UnauthorizedAccessException;
import com.hikmetsuicmez.eventverse.dto.response.EventFilterResponse;
import com.hikmetsuicmez.eventverse.dto.response.EventLocationResponse;
import com.hikmetsuicmez.eventverse.mapper.EventLocationMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.util.Map;
import org.springframework.scheduling.annotation.Scheduled;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;
    private final UserService userService;
    private final NotificationService notificationService;
    private final EventLocationMapper eventLocationMapper;
    private final Cloudinary cloudinary;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private User getCurrentUser() {
        return userService.getCurrentUser();
    }

    @Transactional
    public EventResponse createEvent(EventRequest eventRequest) {
        User currentUser = userService.getCurrentUser();

        Event event = eventMapper.toEntity(eventRequest);
        event.setOrganizer(currentUser);
        event.setRequiresApproval(eventRequest.isRequiresApproval());
        
        // Timezone ayarlaması
        ZoneId turkeyZone = ZoneId.of("Europe/Istanbul");
        LocalDate localDate = event.getDate();
        if (localDate != null) {
            ZonedDateTime zonedDateTime = localDate.atStartOfDay(turkeyZone);
            event.setDate(zonedDateTime.toLocalDate());
        }

        Event savedEvent = eventRepository.save(event);
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

    @Transactional
    public String uploadEventImage(UUID eventId, MultipartFile image) throws IOException {
        Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new ResourceNotFoundException("Etkinlik bulunamadı"));

        // Cloudinary'ye yükle
        Map<?, ?> uploadResult = cloudinary.uploader().upload(image.getBytes(), 
            ObjectUtils.asMap("folder", "events"));

        // Cloudinary URL'ini al
        String imageUrl = uploadResult.get("secure_url").toString();
        
        // Event'i güncelle
        event.setImageUrl(imageUrl);
        eventRepository.save(event);

        return imageUrl;
    }

    @Transactional
    public EventResponse updateEvent(UUID eventId, EventRequest eventRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (!event.getOrganizer().getId().equals(getCurrentUser().getId())) {
            throw new UnauthorizedAccessException("You can only update your own events");
        }

        // Etkinlik bilgilerini güncelle
        event.setTitle(eventRequest.getTitle());
        event.setDescription(eventRequest.getDescription());
        event.setDate(eventRequest.getDate());
        event.setEventTime(eventRequest.getEventTime());
        event.setLocation(eventRequest.getLocation());
        event.setAddress(eventRequest.getAddress());
        event.setMaxParticipants(eventRequest.getMaxParticipants());
        event.setCategory(eventRequest.getCategory());
        event.setHasAgeLimit(eventRequest.isHasAgeLimit());
        event.setAgeLimit(eventRequest.getAgeLimit());
        event.setPaid(eventRequest.isPaid());
        event.setPrice(eventRequest.getPrice());
        event.setRequiresApproval(eventRequest.isRequiresApproval());

        Event updatedEvent = eventRepository.save(event);

        // Katılımcılara bildirim gönder
        notificationService.createEventUpdateNotification(updatedEvent);

        return eventMapper.toResponse(updatedEvent);
    }

    @Transactional
    public void deleteEvent(UUID eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (!event.getOrganizer().getId().equals(getCurrentUser().getId())) {
            throw new UnauthorizedAccessException("You can only delete your own events");
        }

        // Katılımcılara iptal bildirimi gönder
        notificationService.createEventCancellationNotification(event);

        // Etkinliği sil
        eventRepository.delete(event);
    }

    // Etkinlik hatırlatma bildirimi gönder (Scheduled task)
    @Scheduled(cron = "0 0 9 * * *") // Her gün saat 09:00'da çalışır
    public void sendEventReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Event> tomorrowEvents = eventRepository.findByDate(tomorrow);
        
        for (Event event : tomorrowEvents) {
            notificationService.createEventReminderNotification(event);
        }
    }

}
