package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;
import org.acme.dto.CreateUserDto;
import org.acme.dto.UserDto;
import org.acme.exception.ApiException;
import org.acme.exception.generic.ConflictException;
import org.acme.shared.service.SharedService;
import org.jspecify.annotations.NonNull;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import org.acme.enums.Role;

import java.util.List;

@ApplicationScoped
public class UserAuthenticationService extends SharedService {

    public UserDto registerUser(CreateUserDto dto) {
        var response = realm().users().create(getUserRepresentation(dto));

        switch (response.getStatus()) {
            case 201 -> { /* continue */ }
            case 409 -> throw new ConflictException(
                    "User already exists: " + dto.email());
            case 400 -> throw new BadRequestException("Invalid user data");
            default  -> throw new ApiException(Response.Status.INTERNAL_SERVER_ERROR,
                    "Unexpected error: " + response.getStatus());
        }

        String userId = response.getLocation().getPath()
                .replaceAll(".*/([^/]+)$", "$1");

        assignRole(userId, Role.valueOf(dto.role() != null
                ? dto.role().name()
                : Role.RESPONDER.name()));

        return getUserById(userId);
    }

    private static @NonNull UserRepresentation getUserRepresentation(CreateUserDto dto) {
        UserRepresentation user = new UserRepresentation();
        user.setEmail(dto.email());
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setUsername(dto.username());

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(dto.password());
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));
        return user;
    }
}
