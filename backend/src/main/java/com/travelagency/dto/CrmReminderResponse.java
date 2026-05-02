package com.travelagency.dto;

import java.util.UUID;

public record CrmReminderResponse(String type, String message, UUID bookingId, UUID taskId, String dueDate) {}
