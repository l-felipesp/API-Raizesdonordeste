package com.raizesdonordeste.backend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ProdutoRequest(
        @Schema(example = "Cuscuz de Milho com Ovo") @NotBlank String nome,
        @Schema(example = "Cuscuz nordestino com ovo e manteiga de garrafa") String descricao,
        @Schema(example = "14.90") @NotNull @DecimalMin(value = "0.0", inclusive = false, message = "O preço deve ser maior que zero") BigDecimal preco,
        @Schema(example = "Salgados") String categoria,
        @Schema(description = "Opcional. Início da disponibilidade de um produto sazonal (yyyy-MM-dd)") LocalDate disponivelDe,
        @Schema(description = "Opcional. Fim da disponibilidade de um produto sazonal (yyyy-MM-dd)") LocalDate disponivelAte
) {
    @JsonIgnore
    @AssertTrue(message = "disponivelAte deve ser igual ou posterior a disponivelDe")
    public boolean isPeriodoValido() {
        return disponivelDe == null || disponivelAte == null || !disponivelAte.isBefore(disponivelDe);
    }
}
