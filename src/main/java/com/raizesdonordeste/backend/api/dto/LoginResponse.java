package com.raizesdonordeste.backend.api.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresIn, UsuarioResumoDTO user) {}