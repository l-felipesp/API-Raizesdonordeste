package com.raizesdonordeste.backend.api.dto;

import com.raizesdonordeste.backend.domain.model.Produto;
import java.math.BigDecimal;

public record ProdutoResponse(Long id, String nome, String descricao, BigDecimal preco, String categoria) {
    public static ProdutoResponse from(Produto p) {
        return new ProdutoResponse(p.getId(), p.getNome(), p.getDescricao(), p.getPreco(), p.getCategoria());
    }
}