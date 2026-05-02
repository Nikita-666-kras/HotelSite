package com.travelagency.dto;

import com.travelagency.domain.Role;
import java.util.UUID;

public record UserResponse(UUID id, String email, String fullName, String phone, Role role) {}
