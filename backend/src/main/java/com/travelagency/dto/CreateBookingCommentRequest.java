package com.travelagency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateBookingCommentRequest(@NotBlank @Size(max = 3000) String body) {}
