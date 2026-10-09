package com.raizesdonordeste.backend.api.dto;

import com.raizesdonordeste.backend.domain.model.Produto;
import java.math.BigDecimal;
import java.time.LocalDate;

public record ProdutoResponse(Long id, String nome, String descricao, BigDecimal preco, String categoria,
                              LocalDate disponivelDe, LocalDate disponivelAte) {
    public static ProdutoResponse from(Produto p) {
        return new ProdutoResponse(p.getId(), p.getNome(), p.getDescricao(), p.getPreco(), p.getCategoria(),
                p.getDisponivelDe(), p.getDisponivelAte());
    }
}
