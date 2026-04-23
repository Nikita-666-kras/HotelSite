package com.travelagency.dto;

import jakarta.validation.constraints.Size;

public record RailRegistrationData(
        @Size(max = 120) String passengerDocNumber,
        @Size(max = 80) String preferredSeat,
        @Size(max = 120) String wagonPreferences,
        @Size(max = 300) String notes) {}
