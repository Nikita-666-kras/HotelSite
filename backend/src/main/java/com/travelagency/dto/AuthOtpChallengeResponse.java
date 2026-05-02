package com.travelagency.dto;

public record AuthOtpChallengeResponse(String email, String purpose, int expiresInSeconds) {}
