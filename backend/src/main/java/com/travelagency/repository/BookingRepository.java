package com.travelagency.repository;

import com.travelagency.domain.Booking;
import com.travelagency.domain.BookingStatus;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @EntityGraph(attributePaths = {"tour", "user", "assignedManager", "participants"})
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"tour", "user", "assignedManager", "participants"})
    List<Booking> findByStatusOrderByCreatedAtAsc(BookingStatus status);

    @EntityGraph(attributePaths = {"tour", "user", "assignedManager", "participants"})
    List<Booking> findByAssignedManagerIdOrderByUpdatedAtDesc(Long managerId);

    @EntityGraph(attributePaths = {"tour", "user", "assignedManager", "participants"})
    List<Booking> findAllByOrderByCreatedAtDesc();
}
