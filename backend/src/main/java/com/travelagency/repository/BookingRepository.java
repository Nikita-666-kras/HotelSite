package com.travelagency.repository;

import com.travelagency.domain.Booking;
import com.travelagency.domain.BookingStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;

public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    @Override
    @EntityGraph(attributePaths = {"tour", "user", "assignedManager", "participants"})
    @NonNull
    Page<Booking> findAll(Specification<Booking> spec, @NonNull Pageable pageable);

    @EntityGraph(attributePaths = {"tour", "user", "assignedManager", "participants"})
    List<Booking> findByUserIdOrderByCreatedAtDesc(UUID userId);

    @EntityGraph(attributePaths = {"tour", "user", "assignedManager", "participants"})
    List<Booking> findByStatusOrderByCreatedAtAsc(BookingStatus status);

    @EntityGraph(attributePaths = {"tour", "user", "assignedManager", "participants"})
    List<Booking> findByAssignedManagerIdOrderByUpdatedAtDesc(UUID managerId);

    @EntityGraph(attributePaths = {"tour", "user", "assignedManager", "participants"})
    List<Booking> findAllByOrderByCreatedAtDesc();

    @Modifying
    @Query("UPDATE Booking b SET b.assignedManager = null WHERE b.assignedManager.id = :managerId")
    void clearAssignedManagerById(@Param("managerId") UUID managerId);
}
