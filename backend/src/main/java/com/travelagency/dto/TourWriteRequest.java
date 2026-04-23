package com.travelagency.dto;

import com.travelagency.domain.TourCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record TourWriteRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 220) String slug,
        @NotBlank @Size(max = 4000) String description,
        @NotBlank @Size(max = 200) String destination,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        @Min(1) int maxParticipants,
        @NotNull TourCategory category,
        boolean featured,
        List<@Size(max = 200) String> hotels,
        List<@Size(max = 200) String> carriers,
        List<@Size(max = 300) String> tickets,
        List<@Size(max = 300) String> excursions,
        List<@Size(max = 300) String> layovers,
        boolean enableFlightRegistration,
        boolean enableHotelRegistration,
        boolean enableRailRegistration) {}
