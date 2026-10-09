package com.raizesdonordeste.backend.api.dto;

import jakarta.validation.constraints.NotNull;

public record ConsentimentoRequest(@NotNull Boolean aceitaFidelizacao) {}
