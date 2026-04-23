package com.travelagency.dto;

import com.travelagency.domain.ProductType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record TravelProductManagerResponse(
        Long id,
        ProductType type,
        String name,
        String description,
        String origin,
        String destination,
        LocalDateTime departAt,
        LocalDateTime arriveAt,
        LocalDate checkIn,
        LocalDate checkOut,
        BigDecimal price,
        Integer stars,
        String carrier,
        String externalRef,
        List<String> imageUrls,
        List<String> videoUrls,
        List<String> imageObjectKeys,
        List<String> videoObjectKeys) {

    public static TravelProductManagerResponse of(TravelProductResponse view, List<String> imageKeys, List<String> videoKeys) {
        return new TravelProductManagerResponse(
                view.id(),
                view.type(),
                view.name(),
                view.description(),
                view.origin(),
                view.destination(),
                view.departAt(),
                view.arriveAt(),
                view.checkIn(),
                view.checkOut(),
                view.price(),
                view.stars(),
                view.carrier(),
                view.externalRef(),
                view.imageUrls(),
                view.videoUrls(),
                imageKeys != null ? imageKeys : List.of(),
                videoKeys != null ? videoKeys : List.of());
    }
}
