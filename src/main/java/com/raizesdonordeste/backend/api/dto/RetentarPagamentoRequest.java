package com.raizesdonordeste.backend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record RetentarPagamentoRequest(@Schema(example = "PIX") @NotBlank String formaPagamento) {}