package org.acme.exception.generic;

import jakarta.ws.rs.core.Response;
import org.acme.exception.ApiException;

public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(Response.Status.CONFLICT, message);
    }
}
