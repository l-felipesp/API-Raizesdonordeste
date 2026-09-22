package com.raizesdonordeste.backend.api.dto;

import com.raizesdonordeste.backend.domain.enums.TipoMovimentacao;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MovimentacaoEstoqueRequest(@NotNull TipoMovimentacao tipo, @Min(1) int quantidade) {}