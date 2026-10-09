package com.raizesdonordeste.backend.infrastructure.persistence;

import java.math.BigDecimal;

public interface VendaPorUnidadeProjection {
    Long getUnidadeId();
    String getUnidade();
    String getEstado();
    Long getQuantidadePedidos();
    BigDecimal getFaturamento();
}
