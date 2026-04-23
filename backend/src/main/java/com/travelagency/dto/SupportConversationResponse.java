package com.travelagency.dto;

import java.time.Instant;
import java.util.List;

public record SupportConversationResponse(
        Long id, String subject, Instant createdAt, List<SupportMessageResponse> messages) {}
