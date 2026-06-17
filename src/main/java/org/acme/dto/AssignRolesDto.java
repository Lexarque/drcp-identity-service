package org.acme.dto;

import jakarta.validation.constraints.NotEmpty;
import org.acme.enums.Role;

public record AssignRolesDto(
        @NotEmpty(message = "Roles list cannot be empty")
        Role role
) {}
