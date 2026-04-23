package com.travelagency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record ParticipantRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotNull LocalDate dateOfBirth,
        boolean child,
        @Size(max = 20) String passportSeries,
        @Size(max = 50) String passportNumber,
        LocalDate passportIssueDate,
        @Size(max = 100) String birthCertificateNumber,
        @Size(max = 32) String phone,
        @Size(max = 500) String comment) {}
