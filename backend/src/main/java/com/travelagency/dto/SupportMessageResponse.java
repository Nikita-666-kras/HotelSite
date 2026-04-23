package com.travelagency.dto;

import java.time.Instant;

public record SupportMessageResponse(
        Long id, String body, boolean staffReply, String authorEmail, Instant createdAt) {}
