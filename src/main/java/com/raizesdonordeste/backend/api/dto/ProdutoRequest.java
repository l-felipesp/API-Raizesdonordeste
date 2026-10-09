package com.raizesdonordeste.backend.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ProdutoRequest(
        @NotBlank String nome,
        String descricao,
        @NotNull @DecimalMin(value = "0.0", inclusive = false, message = "O preço deve ser maior que zero") BigDecimal preco,
        String categoria,
        LocalDate disponivelDe,
        LocalDate disponivelAte
) {
    @JsonIgnore
    @AssertTrue(message = "disponivelAte deve ser igual ou posterior a disponivelDe")
    public boolean isPeriodoValido() {
        return disponivelDe == null || disponivelAte == null || !disponivelAte.isBefore(disponivelDe);
    }
}
