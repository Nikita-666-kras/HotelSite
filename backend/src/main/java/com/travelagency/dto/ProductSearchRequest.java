package com.travelagency.dto;

import com.travelagency.domain.ProductType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ProductSearchRequest(
        ProductType type,
        String origin,
        String destination,
        BigDecimal maxPrice,
        LocalDateTime departAfter,
        LocalDate checkInFrom) {}
