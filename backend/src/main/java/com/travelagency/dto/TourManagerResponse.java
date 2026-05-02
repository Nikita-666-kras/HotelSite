package com.travelagency.dto;

import com.travelagency.domain.TourCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Tour + raw MinIO keys for editing media in manager UI. */
public record TourManagerResponse(
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
        List<String> mediaObjectKeys,
        boolean featured,
        List<String> hotels,
        List<String> carriers,
        List<String> tickets,
        List<String> excursions,
        List<String> layovers,
        boolean enableFlightRegistration,
        boolean enableHotelRegistration,
        boolean enableRailRegistration) {

    public static TourManagerResponse of(TourResponse base, List<String> mediaObjectKeys) {
        return new TourManagerResponse(
                base.id(),
                base.title(),
                base.slug(),
                base.description(),
                base.destination(),
                base.startDate(),
                base.endDate(),
                base.price(),
                base.maxParticipants(),
                base.category(),
                base.mediaUrls(),
                mediaObjectKeys,
                base.featured(),
                base.hotels(),
                base.carriers(),
                base.tickets(),
                base.excursions(),
                base.layovers(),
                base.enableFlightRegistration(),
                base.enableHotelRegistration(),
                base.enableRailRegistration());
    }
}
