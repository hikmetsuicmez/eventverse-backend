package com.hikmetsuicmez.eventverse.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hikmetsuicmez.eventverse.dto.request.FavoriteRequest;
import com.hikmetsuicmez.eventverse.dto.response.FavoriteResponse;
import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.Favorite;
import com.hikmetsuicmez.eventverse.entity.User;
import com.hikmetsuicmez.eventverse.exception.ResourceNotFoundException;
import com.hikmetsuicmez.eventverse.repository.FavoriteRepository;

import com.hikmetsuicmez.eventverse.repository.EventRepository;
import com.hikmetsuicmez.eventverse.mapper.FavoriteMapper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final UserService userService;
    private final EventRepository eventRepository;
    private final FavoriteMapper favoriteMapper;
    private final NotificationService notificationService;

    public FavoriteResponse addFavorite(FavoriteRequest favoriteRequest) {
        User currentUser = userService.getCurrentUser();
        Event event = eventRepository.findById(favoriteRequest.getEventId())
            .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        Favorite favorite = favoriteMapper.toEntity(favoriteRequest);
        favorite.setUser(currentUser);
        favorite.setEvent(event);

        Favorite savedFavorite = favoriteRepository.save(favorite);
        notificationService.createFavoriteNotification(savedFavorite);

        return favoriteMapper.toResponse(savedFavorite);
    }

    public List<FavoriteResponse> getFavorites() {
        UUID userId = userService.getCurrentUserId();
        List<Favorite> favorites = favoriteRepository.findByUserId(userId);
        return favorites.stream()
            .map(favoriteMapper::toResponse)
            .toList();
    }

    public void deleteFavorite(UUID favoriteId) {
        Favorite favorite = favoriteRepository.findById(favoriteId)
            .orElseThrow(() -> new ResourceNotFoundException("Favorite not found"));
        
        favoriteRepository.deleteById(favoriteId);
        notificationService.createFavoriteDeleteNotification(favorite);
    }
}

