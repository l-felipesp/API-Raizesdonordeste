package com.raizesdonordeste.backend.api.dto;

import com.raizesdonordeste.backend.domain.enums.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusRequest(@NotNull StatusPedido novoStatus) {}