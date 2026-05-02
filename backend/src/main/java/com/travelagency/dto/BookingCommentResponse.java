package com.travelagency.dto;

import java.time.Instant;
import java.util.UUID;

public record BookingCommentResponse(UUID id, String body, UserResponse author, Instant createdAt) {}
