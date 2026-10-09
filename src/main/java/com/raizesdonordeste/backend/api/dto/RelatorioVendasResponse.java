package com.raizesdonordeste.backend.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RelatorioVendasResponse(
        LocalDate inicio,
        LocalDate fim,
        long totalPedidos,
        BigDecimal faturamentoTotal,
        List<VendaPorUnidade> vendasPorUnidade,
        List<VendaPorEstado> vendasPorEstado,
        List<ProdutoMaisVendido> produtosMaisVendidos
) {
    public record VendaPorUnidade(Long unidadeId, String unidade, String estado, long quantidadePedidos,
                                  BigDecimal faturamento) {}

    public record VendaPorEstado(String estado, long quantidadePedidos, BigDecimal faturamento) {}

    public record ProdutoMaisVendido(Long produtoId, String produto, long quantidadeVendida, BigDecimal faturamento) {}
}
