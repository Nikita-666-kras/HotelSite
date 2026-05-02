package com.travelagency.repository;

import com.travelagency.domain.ProductBooking;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductBookingRepository extends JpaRepository<ProductBooking, UUID> {

    @EntityGraph(attributePaths = {"product", "user"})
    List<ProductBooking> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
