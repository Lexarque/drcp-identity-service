package org.acme.resource;

import org.acme.dto.CreateUserDto;
import org.acme.dto.UpdateUserDto;
import org.acme.dto.UserDto;
import org.acme.service.UserAdminService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
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
    public UserDto getUserById(@PathParam("id") String id) {
        return userAdminService.getUserById(id);
    }

    @POST
    public Response createUser(@Valid CreateUserDto dto) {
        UserDto created = userAdminService.createUser(dto);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    @PUT
    @Path("/{id}")
    public UserDto updateUser(@PathParam("id") String id, @Valid UpdateUserDto dto) {
        return userAdminService.updateUser(id, dto);
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") String id) {
        userAdminService.deleteUser(id);
        return Response.noContent().build();
    }
}