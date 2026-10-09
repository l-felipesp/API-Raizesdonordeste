package com.raizesdonordeste.backend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PedidoItemRequest(@Schema(example = "1") @NotNull Long produtoId, @Schema(example = "2") @Min(1) int quantidade) {}