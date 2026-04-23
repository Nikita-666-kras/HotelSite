package com.travelagency.dto;

import com.travelagency.domain.Role;

public record UserResponse(Long id, String email, String fullName, String phone, Role role) {}
