package com.raizesdonordeste.backend.api.dto;

import com.raizesdonordeste.backend.domain.model.Estoque;

public record EstoqueResponse(Long id, Long unidadeId, Long produtoId, String nomeProduto, int quantidade) {
    public static EstoqueResponse from(Estoque e) {
        return new EstoqueResponse(e.getId(), e.getUnidade().getId(), e.getProduto().getId(),
                e.getProduto().getNome(), e.getQuantidade());
    }
}