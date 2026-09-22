package com.raizesdonordeste.backend.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EstoqueRequest(
        @NotNull Long unidadeId,
        @NotNull Long produtoId,
        @Min(0) int quantidadeInicial
) {}