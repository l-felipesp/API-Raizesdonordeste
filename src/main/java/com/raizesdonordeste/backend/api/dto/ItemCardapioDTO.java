package com.raizesdonordeste.backend.api.dto;

import java.math.BigDecimal;

public record ItemCardapioDTO(
        Long produtoId, String nome, String descricao,
        BigDecimal preco, String categoria, int quantidadeDisponivel
) {}