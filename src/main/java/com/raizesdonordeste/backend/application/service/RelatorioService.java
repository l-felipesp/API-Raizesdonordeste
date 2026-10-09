package com.raizesdonordeste.backend.application.service;

import com.raizesdonordeste.backend.api.dto.RelatorioVendasResponse;
import com.raizesdonordeste.backend.api.dto.RelatorioVendasResponse.ProdutoMaisVendido;
import com.raizesdonordeste.backend.api.dto.RelatorioVendasResponse.VendaPorEstado;
import com.raizesdonordeste.backend.api.dto.RelatorioVendasResponse.VendaPorUnidade;
import com.raizesdonordeste.backend.infrastructure.persistence.PedidoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioService {

    private final PedidoRepository pedidoRepository;

    public RelatorioService(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    // Vendas efetivas (pagamento aprovado, pedido não cancelado) entre as datas, inclusive.
    @Transactional(readOnly = true)
    public RelatorioVendasResponse gerarRelatorioVendas(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDoPeriodo = inicio.atStartOfDay();
        LocalDateTime fimExclusivo = fim.plusDays(1).atStartOfDay();

        List<VendaPorUnidade> porUnidade = pedidoRepository.somarVendasPorUnidade(inicioDoPeriodo, fimExclusivo)
                .stream()
                .map(v -> new VendaPorUnidade(v.getUnidadeId(), v.getUnidade(), v.getEstado(),
                        v.getQuantidadePedidos(), v.getFaturamento()))
                .toList();

        List<ProdutoMaisVendido> produtos = pedidoRepository.listarProdutosMaisVendidos(inicioDoPeriodo, fimExclusivo)
                .stream()
                .map(p -> new ProdutoMaisVendido(p.getProdutoId(), p.getProduto(),
                        p.getQuantidadeVendida(), p.getFaturamento()))
                .toList();

        long totalPedidos = porUnidade.stream().mapToLong(VendaPorUnidade::quantidadePedidos).sum();
        BigDecimal faturamentoTotal = porUnidade.stream()
                .map(VendaPorUnidade::faturamento)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new RelatorioVendasResponse(inicio, fim, totalPedidos, faturamentoTotal,
                porUnidade, agruparPorEstado(porUnidade), produtos);
    }

    // "Região" no relatório = estado (UF) da unidade.
    private List<VendaPorEstado> agruparPorEstado(List<VendaPorUnidade> porUnidade) {
        Map<String, long[]> pedidosPorEstado = new LinkedHashMap<>();
        Map<String, BigDecimal> faturamentoPorEstado = new LinkedHashMap<>();
        for (VendaPorUnidade venda : porUnidade) {
            pedidosPorEstado.computeIfAbsent(venda.estado(), e -> new long[1])[0] += venda.quantidadePedidos();
            faturamentoPorEstado.merge(venda.estado(), venda.faturamento(), BigDecimal::add);
        }
        return faturamentoPorEstado.entrySet().stream()
                .map(e -> new VendaPorEstado(e.getKey(), pedidosPorEstado.get(e.getKey())[0], e.getValue()))
                .sorted(Comparator.comparing(VendaPorEstado::faturamento).reversed()
                        .thenComparing(VendaPorEstado::estado))
                .toList();
    }
}
