package com.raizesdonordeste.backend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.raizesdonordeste.backend.domain.enums.TipoMovimentacao;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MovimentacaoEstoqueRequest(@Schema(example = "ENTRADA") @NotNull TipoMovimentacao tipo, @Schema(example = "10") @Min(1) int quantidade) {}