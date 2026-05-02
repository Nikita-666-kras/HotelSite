package com.travelagency.repository;

import com.travelagency.domain.Tour;
import com.travelagency.domain.TourCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TourRepository extends JpaRepository<Tour, UUID> {

    Optional<Tour> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);

    List<Tour> findAllByOrderByIdDesc();

    @Query(
            """
            SELECT t FROM Tour t WHERE
            (:destinationPattern IS NULL OR LOWER(t.destination) LIKE :destinationPattern)
            AND (:category IS NULL OR t.category = :category)
            AND (:from IS NULL OR t.startDate >= :from)
            AND (:to IS NULL OR t.endDate <= :to)
            AND (:maxPrice IS NULL OR t.price <= :maxPrice)
            AND (:featured IS NULL OR t.featured = :featured)
            ORDER BY t.startDate ASC
            """)
    List<Tour> search(
            @Param("destinationPattern") String destinationPattern,
            @Param("category") TourCategory category,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("featured") Boolean featured);

    @Query(
            """
            SELECT DISTINCT t.destination FROM Tour t
            WHERE LOWER(t.destination) LIKE :pattern
            ORDER BY t.destination
            """)
    List<String> findDistinctDestinationsMatching(@Param("pattern") String pattern);
}
