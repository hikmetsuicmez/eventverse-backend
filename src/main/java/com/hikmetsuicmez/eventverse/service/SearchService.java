package com.hikmetsuicmez.eventverse.service;

import com.hikmetsuicmez.eventverse.dto.response.EventFilterResponse;
import com.hikmetsuicmez.eventverse.dto.response.SearchResponse;
import com.hikmetsuicmez.eventverse.dto.response.UserResponse;
import com.hikmetsuicmez.eventverse.entity.Event;
import com.hikmetsuicmez.eventverse.entity.User;
import com.hikmetsuicmez.eventverse.mapper.EventMapper;
import com.hikmetsuicmez.eventverse.mapper.UserMapper;
import com.hikmetsuicmez.eventverse.repository.EventRepository;
import com.hikmetsuicmez.eventverse.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;

    public SearchResponse search(String query) {
        String searchQuery = "%" + query.toLowerCase() + "%";

        List<Event> events = eventRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                query, query);
        List<User> users = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                query, query, query);

        List<EventFilterResponse> eventResponses = events.stream()
                .map(eventMapper::toEventFilterResponse)
                .collect(Collectors.toList());

        List<UserResponse> userResponses = users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());

        return SearchResponse.builder()
                .events(eventResponses)
                .users(userResponses)
                .build();
    }
} 