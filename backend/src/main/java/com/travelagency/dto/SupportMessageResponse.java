package com.travelagency.dto;

import java.time.Instant;
import java.util.UUID;

public record SupportMessageResponse(
        UUID id, String body, boolean staffReply, String authorEmail, Instant createdAt) {}
