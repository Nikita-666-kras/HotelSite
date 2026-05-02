package com.travelagency.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AdminUpdateUserRequest(
        @Size(max = 200) String fullName,
        @Size(max = 32) String phone,
        @Email @Size(max = 255) String email,
        Boolean enabled) {}
