package com.raizesdonordeste.backend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EstoqueRequest(
        @Schema(example = "3") @NotNull Long unidadeId,
        @Schema(example = "7") @NotNull Long produtoId,
        @Schema(example = "20") @Min(0) int quantidadeInicial
) {}