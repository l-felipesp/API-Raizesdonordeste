package com.raizesdonordeste.backend.api.dto;

import com.raizesdonordeste.backend.domain.enums.CanalPedido;
import com.raizesdonordeste.backend.domain.enums.StatusPedido;
import com.raizesdonordeste.backend.domain.model.Pedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(Long id, CanalPedido canalPedido, StatusPedido status, BigDecimal total,
                              LocalDateTime criadoEm, List<ItemPedidoResponse> itens, PagamentoResponse pagamento) {
    public static PedidoResponse from(Pedido pedido) {
        return new PedidoResponse(
                pedido.getId(), pedido.getCanalPedido(), pedido.getStatus(), pedido.getTotal(),
                pedido.getCriadoEm(),
                pedido.getItens().stream().map(ItemPedidoResponse::from).toList(),
                pedido.getPagamento() != null ? PagamentoResponse.from(pedido.getPagamento()) : null
        );
    }
}