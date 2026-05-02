package com.travelagency.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RequestLoginOtpRequest(@Email @NotBlank String email, @NotBlank String password) {}
