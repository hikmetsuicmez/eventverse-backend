package com.hikmetsuicmez.eventverse.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.hikmetsuicmez.eventverse.entity.Event;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Order;

@Repository
public class EventRepositoryCustomImpl implements EventRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Event> filterEvents(String searchText, LocalDate startDate, LocalDate endDate, List<String> categories,
            String location, Double minPrice, Double maxPrice, Integer minAge, Integer maxAge, Boolean isPaid,
            Boolean hasAgeLimit, Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Event> cq = cb.createQuery(Event.class);
        Root<Event> event = cq.from(Event.class);

        List<Predicate> predicates = new ArrayList<>();

        // Search text için title ve description'da arama
        if (searchText != null && !searchText.trim().isEmpty()) {
            String searchPattern = "%" + searchText.toLowerCase().trim() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(event.get("title")), searchPattern),
                cb.like(cb.lower(event.get("description")), searchPattern)
            ));
        }

        // Tarih filtreleri
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(event.get("date"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(event.get("date"), endDate));
        }

        // Kategoriler
        if (categories != null && !categories.isEmpty()) {
            predicates.add(event.get("category").in(categories));
        }

        // Lokasyon filtresi
        if (location != null && !location.trim().isEmpty()) {
            predicates.add(cb.equal(cb.lower(event.get("location")), location.toLowerCase().trim()));
        }

        // Fiyat aralığı
        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(event.get("price"), minPrice));
        }
        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(event.get("price"), maxPrice));
        }

        // Yaş sınırı
        if (minAge != null) {
            predicates.add(cb.greaterThanOrEqualTo(event.get("ageLimit"), minAge));
        }
        if (maxAge != null) {
            predicates.add(cb.lessThanOrEqualTo(event.get("ageLimit"), maxAge));
        }

        // Boolean filtreler
        if (isPaid != null) {
            predicates.add(cb.equal(event.get("isPaid"), isPaid));
        }
        if (hasAgeLimit != null) {
            predicates.add(cb.equal(event.get("hasAgeLimit"), hasAgeLimit));
        }

        // Filtreleri uygula
        if (!predicates.isEmpty()) {
            cq.where(predicates.toArray(new Predicate[0]));
        }

        // Sıralama
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(sort -> {
                if (sort.getProperty().equals("date")) {
                    if (sort.isAscending()) {
                        orders.add(cb.asc(event.get("date")));
                    } else {
                        orders.add(cb.desc(event.get("date")));
                    }
                } else if (sort.getProperty().equals("title")) {
                    if (sort.isAscending()) {
                        orders.add(cb.asc(event.get("title")));
                    } else {
                        orders.add(cb.desc(event.get("title")));
                    }
                } else if (sort.getProperty().equals("price")) {
                    if (sort.isAscending()) {
                        orders.add(cb.asc(event.get("price")));
                    } else {
                        orders.add(cb.desc(event.get("price")));
                    }
                }
            });
            if (!orders.isEmpty()) {
                cq.orderBy(orders);
            }
        }

        // Toplam kayıt sayısını al
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Event> countRoot = countQuery.from(Event.class);
        countQuery.select(cb.count(countRoot));
        if (!predicates.isEmpty()) {
            countQuery.where(predicates.toArray(new Predicate[0]));
        }
        Long totalRecords = entityManager.createQuery(countQuery).getSingleResult();

        // Sayfalama uygula
        TypedQuery<Event> typedQuery = entityManager.createQuery(cq);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Event> results = typedQuery.getResultList();
        return new PageImpl<>(results, pageable, totalRecords);
    }
}
