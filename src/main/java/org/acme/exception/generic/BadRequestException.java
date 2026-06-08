package org.acme.exception.generic;

import jakarta.ws.rs.core.Response;
import org.acme.exception.ApiException;

public class BadRequestException extends ApiException {
    public BadRequestException(String message) {
        super(Response.Status.BAD_REQUEST, message);
    }
}
