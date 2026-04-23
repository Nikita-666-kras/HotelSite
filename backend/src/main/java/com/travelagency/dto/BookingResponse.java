package com.travelagency.dto;

import com.travelagency.domain.BookingStatus;
import java.time.Instant;
import java.util.List;

public record BookingResponse(
        Long id,
        BookingStatus status,
        String contactPhone,
        String notes,
        FlightRegistrationData flightRegistration,
        HotelRegistrationData hotelRegistration,
        RailRegistrationData railRegistration,
        TourResponse tour,
        UserResponse client,
        UserResponse assignedManager,
        List<ParticipantResponse> participants,
        Instant createdAt,
        Instant updatedAt) {}
