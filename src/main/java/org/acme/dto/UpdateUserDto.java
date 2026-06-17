package org.acme.dto;

import org.acme.enums.Role;

public record UpdateUserDto(
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        Role role
) {}