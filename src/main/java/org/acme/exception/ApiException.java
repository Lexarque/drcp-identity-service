package org.acme.exception;

import jakarta.ws.rs.core.Response.Status;

public class ApiException extends RuntimeException {

    private final Status status;

    public ApiException(Status status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatusCode() { return status.getStatusCode(); }
    public String getError()   { return status.getReasonPhrase(); }
}

