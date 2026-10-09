package com.raizesdonordeste.backend.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.raizesdonordeste.backend.domain.enums.CanalPedido;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record PedidoRequest(
        @Schema(example = "1") @NotNull Long unidadeId,
        @Schema(example = "APP") @NotNull CanalPedido canalPedido,
        @Schema(example = "4", description = "Opcional. Usado pelo ATENDENTE para registrar o pedido em nome de um cliente cadastrado") Long clienteId,
        @NotEmpty @Valid List<PedidoItemRequest> itens,
        @Schema(example = "PIX", description = "Mock: PAGAMENTO_INDISPONIVEL simula falha de comunicação com o gateway") @NotBlank String formaPagamento
) {}
