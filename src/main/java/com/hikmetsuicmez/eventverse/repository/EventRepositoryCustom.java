package com.hikmetsuicmez.eventverse.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.hikmetsuicmez.eventverse.entity.Event;

public interface EventRepositoryCustom {
    Page<Event> filterEvents(String searchText, LocalDate startDate, LocalDate endDate,
                            List<String> categories, String location, Double minPrice,
                            Double maxPrice, Integer minAge, Integer maxAge, Boolean isPaid,
                            Boolean hasAgeLimit,Pageable pageable);
}
