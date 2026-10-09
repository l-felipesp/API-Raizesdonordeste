package com.raizesdonordeste.backend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@Schema(example = "cliente@raizes.com") @NotBlank @Email String email, @Schema(example = "Senha@123") @NotBlank String senha) {}