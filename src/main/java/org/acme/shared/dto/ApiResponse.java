package org.acme.shared.dto;

public record ApiResponse<T>(
        int status,
        String message,
        T data
) {
    public static <T> ApiResponse<T> of(int status, String message, T data) {
        return new ApiResponse<>(status, message, data);
    }

    public static ApiResponse<Void> of(int status, String message) {
        return new ApiResponse<>(status, message, null);
    }
}