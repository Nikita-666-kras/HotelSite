package com.travelagency.dto;

import jakarta.validation.constraints.Size;

public record FlightRegistrationData(
        @Size(max = 120) String passengerDocNumber,
        @Size(max = 120) String citizenship,
        @Size(max = 120) String loyaltyProgram,
        @Size(max = 300) String baggageNotes) {}
