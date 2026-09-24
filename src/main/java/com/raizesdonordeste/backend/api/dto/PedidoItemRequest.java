package com.raizesdonordeste.backend.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record PedidoItemRequest(@NotNull Long produtoId, @Min(1) int quantidade) {}