package com.raizesdonordeste.backend.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UnidadeRequest(
        @NotBlank String nome,
        @NotBlank String cidade,
        @NotBlank String estado,
        @NotBlank
        @Pattern(regexp = "COMPLETA|REDUZIDA", message = "O tipo deve ser COMPLETA ou REDUZIDA")
        String tipo
) {}