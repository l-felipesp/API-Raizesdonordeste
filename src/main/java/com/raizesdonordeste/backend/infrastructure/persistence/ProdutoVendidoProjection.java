package com.raizesdonordeste.backend.infrastructure.persistence;

import java.math.BigDecimal;

public interface ProdutoVendidoProjection {
    Long getProdutoId();
    String getProduto();
    Long getQuantidadeVendida();
    BigDecimal getFaturamento();
}
