package com.travelagency.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record CreateBookingRequest(
        @NotNull UUID tourId,
        @Size(max = 32) String contactPhone,
        @NotEmpty @Valid List<ParticipantRequest> participants,
        @Valid FlightRegistrationData flightRegistration,
        @Valid HotelRegistrationData hotelRegistration,
        @Valid RailRegistrationData railRegistration) {}
