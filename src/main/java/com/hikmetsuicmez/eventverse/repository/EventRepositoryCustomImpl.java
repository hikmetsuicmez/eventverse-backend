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

        List<Predicate> predicates = buildPredicates(cb, event, searchText, startDate, endDate, categories,
                location, minPrice, maxPrice, minAge, maxAge, isPaid, hasAgeLimit);

        if (!predicates.isEmpty()) {
            cq.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Sıralama
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            pageable.getSort().forEach(sort -> {
                String property = sort.getProperty();
                if (property.equals("date") || property.equals("title") || 
                    property.equals("price") || property.equals("maxParticipants")) {
                    if (sort.isAscending()) {
                        orders.add(cb.asc(event.get(property)));
                    } else {
                        orders.add(cb.desc(event.get(property)));
                    }
                }
            });
            if (!orders.isEmpty()) {
                cq.orderBy(orders);
            }
        }

        TypedQuery<Event> query = entityManager.createQuery(cq);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        // Toplam kayıt sayısını hesapla
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Event> countRoot = countQuery.from(Event.class);
        countQuery.select(cb.count(countRoot));

        List<Predicate> countPredicates = buildPredicates(cb, countRoot, searchText, startDate, endDate, categories,
                location, minPrice, maxPrice, minAge, maxAge, isPaid, hasAgeLimit);

        if (!countPredicates.isEmpty()) {
            countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
        }

        Long total = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(query.getResultList(), pageable, total);
    }

    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Event> root,
            String searchText, LocalDate startDate, LocalDate endDate, List<String> categories,
            String location, Double minPrice, Double maxPrice, Integer minAge, Integer maxAge,
            Boolean isPaid, Boolean hasAgeLimit) {
        
        List<Predicate> predicates = new ArrayList<>();

        // Metin araması
        if (searchText != null && !searchText.trim().isEmpty()) {
            String pattern = "%" + searchText.toLowerCase().trim() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("title")), pattern),
                cb.like(cb.lower(root.get("description")), pattern)
            ));
        }

        // Tarih filtreleri
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("date"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("date"), endDate));
        }

        // Kategoriler
        if (categories != null && !categories.isEmpty()) {
            predicates.add(root.get("category").in(categories));
        }

        // Lokasyon
        if (location != null && !location.trim().isEmpty()) {
            predicates.add(cb.equal(cb.lower(root.get("location")), location.toLowerCase().trim()));
        }

        // Fiyat aralığı
        if (minPrice != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
        }
        if (maxPrice != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
        }

        // Yaş sınırı
        if (minAge != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("ageLimit"), minAge));
        }
        if (maxAge != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("ageLimit"), maxAge));
        }

        // Boolean filtreler
        if (isPaid != null) {
            predicates.add(cb.equal(root.get("isPaid"), isPaid));
        }
        if (hasAgeLimit != null) {
            predicates.add(cb.equal(root.get("hasAgeLimit"), hasAgeLimit));
        }

        return predicates;
    }
}
