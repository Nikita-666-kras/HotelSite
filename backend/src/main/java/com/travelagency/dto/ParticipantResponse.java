package com.travelagency.dto;

import java.time.LocalDate;

public record ParticipantResponse(
        Long id,
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
