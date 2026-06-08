package org.acme.dto;

import java.util.List;

public record UpdateUserDto(
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        List<String> roles
) {}