package org.acme.dto;

import org.acme.enums.Role;

import java.util.List;

public record UserDto(
        String id,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        Role role
) {}