package org.acme.service;

import org.acme.dto.CreateUserDto;
import org.acme.dto.UpdateUserDto;
import org.acme.dto.UserDto;
import jakarta.enterprise.context.ApplicationScoped;
import org.acme.enums.Role;
import org.acme.shared.service.SharedService;
import org.jspecify.annotations.NonNull;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

@ApplicationScoped
public class UserAdminService extends SharedService {

    public UserDto createUser(CreateUserDto dto) {
        UserRepresentation user = getUserRepresentation(dto);

        var response = realm().users().create(user);

        String userId = response.getLocation().getPath()
                .replaceAll(".*/([^/]+)$", "$1");

        if (dto.role() != null) {
            assignRole(userId, dto.role());
        } else {
            assignRole(userId, Role.valueOf("VICTIM"));
        }

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

    public UserDto updateUser(String id, UpdateUserDto dto) {
        var userResource = realm().users().get(id);
        UserRepresentation user = userResource.toRepresentation();

        if (dto.email() != null) user.setEmail(dto.email());
        if (dto.firstName() != null) user.setFirstName(dto.firstName());
        if (dto.lastName() != null) user.setLastName(dto.lastName());
        user.setEnabled(dto.enabled());

        userResource.update(user);

        if (dto.role() != null) {
            List<RoleRepresentation> currentRoles = userResource.roles()
                    .realmLevel().listEffective().stream()
                    .filter(r -> !r.getName().startsWith("default-roles")
                            && !r.getName().equals("offline_access")
                            && !r.getName().equals("uma_authorization"))
                    .toList();
            userResource.roles().realmLevel().remove(currentRoles);
            assignRole(id, dto.role());
        }

        return getUserById(id);
    }

    public void deleteUser(String id) {
        realm().users().delete(id);
    }
}
