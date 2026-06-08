package org.acme.exception.generic;

import jakarta.ws.rs.core.Response;
import org.acme.exception.ApiException;

public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(Response.Status.UNAUTHORIZED, message);
    }
}
