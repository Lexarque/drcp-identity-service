package org.acme.exception.generic;

import jakarta.ws.rs.core.Response;
import org.acme.exception.ApiException;

public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(Response.Status.FORBIDDEN, message);
    }
}
