package com.travelagency.dto;

import com.travelagency.domain.CrmTaskStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record CrmTaskResponse(
        UUID id,
        String title,
        String description,
        CrmTaskStatus status,
        LocalDate dueDate,
        UserResponse assignee,
        UUID bookingId,
        String bookingTitle,
        Instant createdAt,
        Instant updatedAt) {}
