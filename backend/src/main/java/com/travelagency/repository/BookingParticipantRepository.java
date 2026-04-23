package com.travelagency.repository;

import com.travelagency.domain.BookingParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingParticipantRepository extends JpaRepository<BookingParticipant, Long> {}
