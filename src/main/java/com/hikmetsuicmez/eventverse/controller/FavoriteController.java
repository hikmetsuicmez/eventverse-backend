package com.hikmetsuicmez.eventverse.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hikmetsuicmez.eventverse.dto.request.FavoriteRequest;
import com.hikmetsuicmez.eventverse.dto.response.ApiResponse;
import com.hikmetsuicmez.eventverse.dto.response.FavoriteResponse;
import com.hikmetsuicmez.eventverse.service.FavoriteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    @PostMapping
    public ApiResponse<FavoriteResponse> addFavorite(@RequestBody @Valid FavoriteRequest favoriteRequest) {
        FavoriteResponse favoriteResponse = favoriteService.addFavorite(favoriteRequest);
        return ApiResponse.success(favoriteResponse);
    }

    @GetMapping
    public ApiResponse<List<FavoriteResponse>> getFavorites() {
        List<FavoriteResponse> favoriteResponses = favoriteService.getFavorites();
        return ApiResponse.success(favoriteResponses);
    }

    @DeleteMapping("/{favoriteId}")
    public ApiResponse<String> deleteFavorite(@PathVariable UUID favoriteId) {
        favoriteService.deleteFavorite(favoriteId);
        return ApiResponse.success("Favorite deleted successfully");
    }

    @GetMapping("/status/{eventId}")
    public ApiResponse<FavoriteResponse> getFavoriteStatus(@PathVariable UUID eventId) {
        FavoriteResponse favoriteResponse = favoriteService.getFavoriteStatus(eventId);
        return ApiResponse.success(favoriteResponse);
    }
}
