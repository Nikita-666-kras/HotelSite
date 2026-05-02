package com.travelagency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.UUID;

public record CreateCrmTaskRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 3000) String description,
        UUID assigneeId,
        UUID bookingId,
        LocalDate dueDate) {}
