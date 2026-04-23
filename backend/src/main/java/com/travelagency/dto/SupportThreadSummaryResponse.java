package com.travelagency.dto;

import java.time.Instant;

public record SupportThreadSummaryResponse(Long id, String subject, Instant createdAt) {}
