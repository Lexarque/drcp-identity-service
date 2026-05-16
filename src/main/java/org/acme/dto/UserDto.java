package org.acme.dto;

import java.util.List;

public record UserDto(
        String id,
        String email,
        String firstName,
        String lastName,
        boolean enabled,
        List<String> roles
) {}
