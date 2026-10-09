package com.raizesdonordeste.backend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CadastroUsuarioRequest(
        @Schema(example = "Maria Silva") @NotBlank String nome,
        @Schema(example = "maria@exemplo.com") @NotBlank @Email String email,
        @Schema(example = "Senha@123", description = "Mínimo de 8 caracteres") @NotBlank @Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres") String senha,
        @Schema(example = "true", description = "Opcional. Consentimento para uso dos dados no programa de fidelização") Boolean aceitaFidelizacao
) {}
