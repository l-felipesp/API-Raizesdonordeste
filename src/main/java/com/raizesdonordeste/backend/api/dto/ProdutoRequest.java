package com.raizesdonordeste.backend.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProdutoRequest(
        @NotBlank String nome,
        String descricao,
        @NotNull @DecimalMin(value = "0.0", inclusive = false, message = "O preço deve ser maior que zero") BigDecimal preco,
        String categoria
) {}