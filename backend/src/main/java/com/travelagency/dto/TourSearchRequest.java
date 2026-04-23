package com.travelagency.dto;

import com.travelagency.domain.TourCategory;
import java.math.BigDecimal;
import java.time.LocalDate;

public record TourSearchRequest(
        String destination,
        TourCategory category,
        LocalDate dateFrom,
        LocalDate dateTo,
        BigDecimal maxPrice,
        Boolean featured) {}
