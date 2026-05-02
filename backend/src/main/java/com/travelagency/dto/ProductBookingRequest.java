package com.travelagency.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ProductBookingRequest(@NotNull UUID productId, @Size(max = 2000) String details) {}
