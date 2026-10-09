package com.raizesdonordeste.backend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record ConsentimentoRequest(@Schema(example = "false", description = "true concede e false revoga o consentimento") @NotNull Boolean aceitaFidelizacao) {}
