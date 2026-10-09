package com.raizesdonordeste.backend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.raizesdonordeste.backend.domain.enums.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusRequest(@Schema(example = "PRONTO") @NotNull StatusPedido novoStatus) {}