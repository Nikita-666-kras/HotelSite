package com.travelagency.dto;

import com.travelagency.domain.CrmTaskStatus;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateCrmTaskRequest(
        @Size(max = 200) String title,
        @Size(max = 3000) String description,
        CrmTaskStatus status,
        LocalDate dueDate) {}
