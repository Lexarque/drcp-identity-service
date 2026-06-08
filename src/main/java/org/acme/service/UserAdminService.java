package org.acme.service;

import jakarta.ws.rs.BadRequestException;
import org.acme.dto.CreateUserDto;
import org.acme.dto.UpdateUserDto;
import org.acme.dto.UserDto;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

@ApplicationScoped
public class UserAdminService {

    @ConfigProperty(name = "keycloak.admin.target-realm")
    String targetRealm;

    @Inject
    Keycloak keycloak;

    private RealmResource realm() {
        return keycloak.realm(targetRealm);
    }

    private UserDto toDto(UserRepresentation user) {
        List<String> roles = realm().users()
                .get(user.getId())
                .roles()
                .realmLevel()
                .listEffective()
                .stream()
                .map(RoleRepresentation::getName)
                .filter(r -> !r.startsWith("default-roles") && !r.equals("offline_access") && !r.equals("uma_authorization"))
                .toList();

        return new UserDto(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                Boolean.TRUE.equals(user.isEnabled()),
                roles
        );
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

    public UserDto createUser(CreateUserDto dto) {
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

        var response = realm().users().create(user);

        String userId = response.getLocation().getPath()
                .replaceAll(".*/([^/]+)$", "$1");

        if (dto.roles() != null && !dto.roles().isEmpty()) {
            assignRoles(userId, dto.roles());
        } else {
            assignRoles(userId, List.of("RESPONDER"));
        }

        return getUserById(userId);
    }

    public UserDto updateUser(String id, UpdateUserDto dto) {
        var userResource = realm().users().get(id);
        UserRepresentation user = userResource.toRepresentation();

        if (dto.email() != null) user.setEmail(dto.email());
        if (dto.firstName() != null) user.setFirstName(dto.firstName());
        if (dto.lastName() != null) user.setLastName(dto.lastName());
        user.setEnabled(dto.enabled());

        userResource.update(user);

        if (dto.roles() != null) {
            List<RoleRepresentation> currentRoles = userResource.roles()
                    .realmLevel().listEffective().stream()
                    .filter(r -> !r.getName().startsWith("default-roles")
                            && !r.getName().equals("offline_access")
                            && !r.getName().equals("uma_authorization"))
                    .toList();
            userResource.roles().realmLevel().remove(currentRoles);
            assignRoles(id, dto.roles());
        }

        return getUserById(id);
    }

    public void deleteUser(String id) {
        realm().users().delete(id);
    }

    public void assignRoles(String userId, List<String> roleNames) {
        var userResource = realm().users().get(userId);

        List<RoleRepresentation> currentRoles = userResource.roles()
                .realmLevel().listEffective().stream()
                .filter(r -> !r.getName().startsWith("default-roles")
                        && !r.getName().equals("offline_access")
                        && !r.getName().equals("uma_authorization"))
                .toList();

        List<RoleRepresentation> newRoles = roleNames.stream()
                .map(roleName -> {
                    try {
                        return realm().roles().get(roleName).toRepresentation();
                    } catch (Exception e) {
                        throw new BadRequestException("Role does not exist: " + roleName);
                    }
                })
                .toList();

        if (roleNames.size() == 1) {
            userResource.roles().realmLevel().remove(currentRoles);
            userResource.roles().realmLevel().add(newRoles);
        } else {
            userResource.roles().realmLevel().add(newRoles);
        }
    }
}
