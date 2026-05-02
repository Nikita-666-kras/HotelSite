package com.travelagency.repository;

import com.travelagency.domain.Booking;
import com.travelagency.domain.BookingStatus;
import com.travelagency.domain.Tour;
import com.travelagency.domain.User;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class BookingSpecifications {

    private BookingSpecifications() {}

    public static Specification<Booking> pipelineFilter(
            BookingStatus status,
            UUID managerId,
            UUID tourId,
            LocalDate createdFrom,
            LocalDate createdTo,
            String q) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (managerId != null) {
                predicates.add(cb.equal(root.get("assignedManager").get("id"), managerId));
            }
            if (tourId != null) {
                predicates.add(cb.equal(root.get("tour").get("id"), tourId));
            }
            if (createdFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"), createdFrom.atStartOfDay(ZoneOffset.UTC).toInstant()));
            }
            if (createdTo != null) {
                predicates.add(cb.lessThan(
                        root.get("createdAt"),
                        createdTo.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant()));
            }
            if (q != null && !q.isBlank()) {
                String trimmed = q.trim();
                try {
                    UUID uuid = UUID.fromString(trimmed);
                    predicates.add(cb.equal(root.get("id"), uuid));
                } catch (IllegalArgumentException ignored) {
                    Subquery<UUID> sub = query.subquery(UUID.class);
                    Root<Booking> subRoot = sub.from(Booking.class);
                    Join<Booking, User> uj = subRoot.join("user", JoinType.INNER);
                    Join<Booking, Tour> tj = subRoot.join("tour", JoinType.INNER);
                    String pattern = "%" + trimmed.toLowerCase() + "%";
                    sub.select(subRoot.get("id"));
                    sub.where(cb.and(
                            cb.equal(subRoot.get("id"), root.get("id")),
                            cb.or(
                                    cb.like(cb.lower(uj.get("email")), pattern),
                                    cb.like(cb.lower(uj.get("fullName")), pattern),
                                    cb.like(cb.lower(tj.get("title")), pattern),
                                    cb.like(cb.lower(subRoot.get("contactPhone")), pattern))));
                    predicates.add(cb.exists(sub));
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
