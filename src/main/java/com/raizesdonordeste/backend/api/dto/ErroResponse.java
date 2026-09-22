package com.raizesdonordeste.backend.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(
        String error,
        String message,
        List<CampoInvalido> details,
        Instant timestamp,
        String path,
        String requestId
) {
    public record CampoInvalido(String field, String issue) {}

    public static ErroResponse de(String error, String message, String path, List<CampoInvalido> details) {
        return new ErroResponse(error, message, details, Instant.now(), path,
                java.util.UUID.randomUUID().toString());
    }

    public static ErroResponse de(String error, String message, String path) {
        return de(error, message, path, List.of());
    }
}