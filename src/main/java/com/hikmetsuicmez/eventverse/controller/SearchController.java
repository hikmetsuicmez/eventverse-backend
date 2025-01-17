package com.hikmetsuicmez.eventverse.controller;

import com.hikmetsuicmez.eventverse.dto.response.ApiResponse;
import com.hikmetsuicmez.eventverse.dto.response.SearchResponse;
import com.hikmetsuicmez.eventverse.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ApiResponse<SearchResponse> search(@RequestParam String query) {
        return ApiResponse.success(searchService.search(query), "Arama sonuçları başarıyla getirildi");
    }
} 