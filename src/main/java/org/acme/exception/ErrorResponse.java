package org.acme.exception;

public record ErrorResponse(
        int status,
        String error,
        String message,
        String path,
        String timestamp
) {
    public static ErrorResponse of(int status, String error,
                                   String message, String path) {
        return new ErrorResponse(
                status,
                message,
                error,
                path,
                java.time.Instant.now().toString()
        );
    }
}