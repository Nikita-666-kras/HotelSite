package com.travelagency.repository;

import com.travelagency.domain.BookingParticipant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingParticipantRepository extends JpaRepository<BookingParticipant, UUID> {}
