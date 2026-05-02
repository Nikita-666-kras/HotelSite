package com.travelagency.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @Size(max = 200) String fullName,
        @Size(max = 32) String phone) {}
