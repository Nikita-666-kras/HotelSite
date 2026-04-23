package com.travelagency.repository;

import com.travelagency.domain.FavoriteTour;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteTourRepository extends JpaRepository<FavoriteTour, Long> {

    @EntityGraph(attributePaths = {"tour"})
    List<FavoriteTour> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndTourId(Long userId, Long tourId);

    Optional<FavoriteTour> findByUserIdAndTourId(Long userId, Long tourId);

    void deleteByUserIdAndTourId(Long userId, Long tourId);
}
