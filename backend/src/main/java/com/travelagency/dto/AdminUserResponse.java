package com.travelagency.dto;

import com.travelagency.domain.Role;
import java.util.UUID;

public record AdminUserResponse(UUID id, String email, String fullName, String phone, Role role, boolean enabled) {}
