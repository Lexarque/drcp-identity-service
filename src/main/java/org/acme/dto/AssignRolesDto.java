package org.acme.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AssignRolesDto(
        @NotEmpty(message = "Roles list cannot be empty")
        List<String> roles
) {}
