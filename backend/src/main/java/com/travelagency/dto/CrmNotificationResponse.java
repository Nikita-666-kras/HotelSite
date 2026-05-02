package com.travelagency.dto;

import java.time.Instant;
import java.util.UUID;

public record CrmNotificationResponse(
        UUID id,
        String channel,
        String subject,
        String body,
        boolean read,
        UserResponse recipient,
        Instant createdAt) {}
