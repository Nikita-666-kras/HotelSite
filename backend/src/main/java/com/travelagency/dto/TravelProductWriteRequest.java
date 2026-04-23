package com.travelagency.dto;

import com.travelagency.domain.ProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TravelProductWriteRequest(
        @NotNull ProductType type,
        @NotBlank @Size(max = 300) String name,
        @Size(max = 2000) String description,
        @NotBlank @Size(max = 200) String origin,
        @NotBlank @Size(max = 200) String destination,
        LocalDateTime departAt,
        LocalDateTime arriveAt,
        LocalDate checkIn,
        LocalDate checkOut,
        @NotNull @DecimalMin("0.01") BigDecimal price,
        Integer stars,
        @Size(max = 120) String carrier,
        @Size(max = 120) String externalRef) {}
