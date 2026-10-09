package com.raizesdonordeste.backend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UnidadeRequest(
        @Schema(example = "Raízes Natal Ponta Negra") @NotBlank String nome,
        @Schema(example = "Natal") @NotBlank String cidade,
        @Schema(example = "RN") @NotBlank String estado,
        @Schema(example = "COMPLETA", allowableValues = {"COMPLETA", "REDUZIDA"})
        @NotBlank
        @Pattern(regexp = "COMPLETA|REDUZIDA", message = "O tipo deve ser COMPLETA ou REDUZIDA")
        String tipo
) {}
