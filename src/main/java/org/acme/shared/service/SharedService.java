package org.acme.shared.service;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.NotFoundException;
import org.acme.dto.UserDto;
import org.acme.enums.Role;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

public class SharedService {
    @ConfigProperty(name = "keycloak.admin.target-realm")
    String targetRealm;

    @Inject
    Keycloak keycloak;

    public RealmResource realm() {
        return keycloak.realm(targetRealm);
    }

    private UserDto toDto(UserRepresentation user) {
        String roleName = realm().users()
                .get(user.getId())
                .roles()
                .realmLevel()
                .listEffective()
                .stream()
                .map(RoleRepresentation::getName)
                .filter(r -> !r.startsWith("default-roles")
                        && !r.equals("offline_access")
                        && !r.equals("uma_authorization"))
                .findFirst()
                .orElse(null);

        Role role = roleName != null ? Role.valueOf(roleName) : null;

        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                Boolean.TRUE.equals(user.isEnabled()),
                role
        );
    }

    public void assignRole(String userId, Role role) {
        var userResource = realm().users().get(userId);

        List<RoleRepresentation> currentRoles = userResource.roles()
                .realmLevel().listEffective().stream()
                .filter(r -> !r.getName().startsWith("default-roles")
                        && !r.getName().equals("offline_access")
                        && !r.getName().equals("uma_authorization"))
                .toList();

        RoleRepresentation newRole;
        try {
            newRole = realm().roles().get(role.name()).toRepresentation();
        } catch (Exception e) {
            throw new BadRequestException("Role does not exist: " + role.name());
        }

        if (!currentRoles.isEmpty()) {
            userResource.roles().realmLevel().remove(currentRoles);
        }
        userResource.roles().realmLevel().add(List.of(newRole));
    }

    public List<UserDto> getAllUsers() {
        return realm().users().list()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public UserDto getUserById(String id) {
        try {
            UserRepresentation user = realm().users().get(id).toRepresentation();
            return toDto(user);
        } catch (Exception e) {
            throw new NotFoundException("User not found: " + id);
        }
    }
}
