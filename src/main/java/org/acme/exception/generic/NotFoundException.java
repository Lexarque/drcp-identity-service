package org.acme.exception.generic;

import jakarta.ws.rs.core.Response;
import org.acme.exception.ApiException;

public class NotFoundException extends ApiException {
    public NotFoundException(String message) {
        super(Response.Status.NOT_FOUND, message);
    }
}
