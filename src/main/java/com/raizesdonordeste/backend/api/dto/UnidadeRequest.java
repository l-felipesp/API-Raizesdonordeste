package com.raizesdonordeste.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public record UnidadeRequest(
        @NotBlank String nome,
        @NotBlank String cidade,
        @NotBlank String estado,
        @NotBlank String tipo
) {}