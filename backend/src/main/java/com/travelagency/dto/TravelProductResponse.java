package com.travelagency.dto;

import com.travelagency.domain.ProductType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record TravelProductResponse(
        UUID id,
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
        List<String> videoUrls) {}
