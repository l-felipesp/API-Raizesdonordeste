package com.raizesdonordeste.backend.api.dto;

import com.raizesdonordeste.backend.domain.enums.CanalPedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PedidoRequest(
        @NotNull Long unidadeId,
        @NotNull CanalPedido canalPedido,
        @NotEmpty @Valid List<PedidoItemRequest> itens,
        @NotBlank String formaPagamento
) {}