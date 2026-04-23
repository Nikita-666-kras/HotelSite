package com.travelagency.dto;

import com.travelagency.domain.BookingStatus;
import jakarta.validation.constraints.Size;

public record ManagerUpdateBookingRequest(
        BookingStatus status,
        Long assignedManagerId,
        @Size(max = 2000) String notes) {}
