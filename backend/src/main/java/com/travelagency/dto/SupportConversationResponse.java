package com.travelagency.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SupportConversationResponse(
        UUID id, String subject, Instant createdAt, List<SupportMessageResponse> messages) {}
