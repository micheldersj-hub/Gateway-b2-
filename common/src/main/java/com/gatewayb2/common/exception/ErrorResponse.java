package com.gatewayb2.common.exception;

import java.time.Instant;
import java.util.List;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String errorCode,
        String message,
        String path,
        List<String> details
) {
    public static ErrorResponse of(int status, String errorCode, String message, String path) {
        return new ErrorResponse(Instant.now(), status, errorCode, message, path, List.of());
    }

    public static ErrorResponse of(int status, String errorCode, String message, String path, List<String> details) {
        return new ErrorResponse(Instant.now(), status, errorCode, message, path, details);
    }
}
