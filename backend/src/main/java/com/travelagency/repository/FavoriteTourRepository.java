package com.travelagency.repository;

import com.travelagency.domain.FavoriteTour;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteTourRepository extends JpaRepository<FavoriteTour, UUID> {

    @EntityGraph(attributePaths = {"tour"})
    List<FavoriteTour> findByUserIdOrderByCreatedAtDesc(UUID userId);

    boolean existsByUserIdAndTourId(UUID userId, UUID tourId);

    Optional<FavoriteTour> findByUserIdAndTourId(UUID userId, UUID tourId);

    void deleteByUserIdAndTourId(UUID userId, UUID tourId);
}
