package com.travelagency.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ParticipantResponse(
        UUID id,
        String firstName,
        String lastName,
        LocalDate dateOfBirth,
        boolean child,
        String passportSeries,
        String passportNumber,
        LocalDate passportIssueDate,
        String birthCertificateNumber,
        String phone,
        String comment) {}
