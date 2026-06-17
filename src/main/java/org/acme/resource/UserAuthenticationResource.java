package org.acme.resource;

import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.acme.shared.dto.ApiResponse;
import org.acme.dto.CreateUserDto;
import org.acme.dto.UserDto;
import org.acme.service.UserAuthenticationService;

import java.time.temporal.ChronoUnit;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserAuthenticationResource {

    @Inject
    UserAuthenticationService userAuthenticationService;

    @POST
    @Path("/register")
    @RateLimit(value = 5, window = 1, windowUnit = ChronoUnit.MINUTES)
    public Response register(@Valid CreateUserDto dto) {
        UserDto created = userAuthenticationService.registerUser(dto);
        return Response.status(Response.Status.CREATED)
                .entity(ApiResponse.of(201, "Registration successful", created))
                .build();
    }
}
