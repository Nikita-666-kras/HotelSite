package com.travelagency.dto;

import jakarta.validation.constraints.Size;

public record HotelRegistrationData(
        @Size(max = 200) String guestFullName,
        @Size(max = 120) String documentNumber,
        @Size(max = 120) String estimatedArrivalTime,
        @Size(max = 500) String specialRequests) {}
