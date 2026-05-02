package com.travelagency.dto;

import java.time.Instant;
import java.util.UUID;

public record SupportThreadSummaryResponse(UUID id, String subject, Instant createdAt) {}
