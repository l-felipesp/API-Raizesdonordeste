package com.raizesdonordeste.backend.api.dto;

import com.raizesdonordeste.backend.domain.model.ItemPedido;
import java.math.BigDecimal;

public record ItemPedidoResponse(Long produtoId, String nomeProduto, int quantidade,
                                  BigDecimal precoUnitario, BigDecimal subtotal) {
    public static ItemPedidoResponse from(ItemPedido item) {
        return new ItemPedidoResponse(item.getProduto().getId(), item.getProduto().getNome(),
                item.getQuantidade(), item.getPrecoUnitario(), item.subtotal());
    }
}