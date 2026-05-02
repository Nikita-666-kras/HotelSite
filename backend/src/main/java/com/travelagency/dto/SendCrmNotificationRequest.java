package com.travelagency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record SendCrmNotificationRequest(
        UUID recipientId,
        @NotBlank @Size(max = 120) String channel,
        @NotBlank @Size(max = 255) String subject,
        @NotBlank @Size(max = 4000) String body) {}
