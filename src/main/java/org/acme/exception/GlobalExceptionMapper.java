package org.acme.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

import java.util.stream.Collectors;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception e) {
        String path = uriInfo != null ? uriInfo.getPath() : "unknown";

        // your custom exceptions — status + message fully automatic
        if (e instanceof ApiException ae) {
            return buildResponse(ae.getStatusCode(), ae.getError(),
                    ae.getMessage(), path);
        }

        // JAX-RS built-in exceptions — also automatic
        if (e instanceof WebApplicationException wae) {
            Status status = Status.fromStatusCode(
                    wae.getResponse().getStatus()
            );
            return buildResponse(
                    status.getStatusCode(),
                    status.getReasonPhrase(),  // ← automatic
                    wae.getMessage(),
                    path
            );
        }

        // validation errors — extract all violations automatically
        if (e instanceof ConstraintViolationException cve) {
            String message = cve.getConstraintViolations()
                    .stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
            return buildResponse(400,
                    Status.BAD_REQUEST.getReasonPhrase(), message, path);
        }

        // unhandled — log full stack trace, return 500
        LOG.errorf(e, "Unhandled exception at %s: %s", path, e.getMessage());
        return buildResponse(
                500,
                Status.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "An unexpected error occurred",
                path
        );
    }

    private Response buildResponse(int status, String error,
                                   String message, String path) {
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(ErrorResponse.of(status, error, message, path))
                .build();
    }
}