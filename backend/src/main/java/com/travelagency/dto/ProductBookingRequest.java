package com.travelagency.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProductBookingRequest(@NotNull Long productId, @Size(max = 2000) String details) {}
