package com.travelagency.dto;

import com.travelagency.domain.TourCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record TourResponse(
        UUID id,
        String title,
        String slug,
        String description,
        String destination,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal price,
        int maxParticipants,
        TourCategory category,
        List<String> mediaUrls,
        boolean featured,
        List<String> hotels,
        List<String> carriers,
        List<String> tickets,
        List<String> excursions,
        List<String> layovers,
        boolean enableFlightRegistration,
        boolean enableHotelRegistration,
        boolean enableRailRegistration) {}
