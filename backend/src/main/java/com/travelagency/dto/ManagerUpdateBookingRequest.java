package com.travelagency.dto;

import com.travelagency.domain.BookingStatus;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ManagerUpdateBookingRequest(
        BookingStatus status,
        UUID assignedManagerId,
        @Size(max = 2000) String notes) {}
