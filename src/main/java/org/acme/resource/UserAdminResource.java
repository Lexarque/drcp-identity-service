package org.acme.resource;

import org.acme.dto.*;
import org.acme.service.UserAdminService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.shared.dto.ApiResponse;

import java.util.List;

@Path("/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("ADMIN")
public class UserAdminResource {

    @Inject
    UserAdminService userAdminService;

    @GET
    public List<UserDto> getAllUsers() {
        return userAdminService.getAllUsers();
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") String id) {
        return Response.ok()
                .entity(ApiResponse.of(200, "User updated successfully", userAdminService.getUserById(id)))
                .build();
    }

    @POST
    public Response createUser(@Valid CreateUserDto dto) {
        UserDto created = userAdminService.createUser(dto);
        return Response.ok()
                .entity(ApiResponse.of(201, "User created successfully", created))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") String id, @Valid UpdateUserDto dto) {
        return Response.ok()
                .entity(ApiResponse.of(200, "User updated successfully", userAdminService.updateUser(id, dto)))
                .build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") String id) {
        userAdminService.deleteUser(id);
        return Response.ok()
                .entity(ApiResponse.of(200, "User deleted successfully"))
                .build();
    }

    @POST
    @Path("/{id}/roles")
    public Response assignRoleToUser(@PathParam("id") String id, @Valid AssignRolesDto dto) {
        userAdminService.assignRole(id, dto.role());
        return Response.ok()
                .entity(ApiResponse.of(200, "Role assigned to user successfully"))
                .build();
    }
}