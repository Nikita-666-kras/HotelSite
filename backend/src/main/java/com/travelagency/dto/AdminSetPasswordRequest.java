package com.travelagency.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminSetPasswordRequest(
        @NotBlank @Size(min = 8, max = 128) String newPassword) {}
