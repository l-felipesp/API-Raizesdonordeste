package com.raizesdonordeste.backend.api.dto;

import jakarta.validation.constraints.NotBlank;

public record RetentarPagamentoRequest(@NotBlank String formaPagamento) {}