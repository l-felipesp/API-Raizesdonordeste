package com.raizesdonordeste.backend.infrastructure.persistence;

import com.raizesdonordeste.backend.domain.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, Long>, JpaSpecificationExecutor<Pedido> {

    // Relatórios da matriz. Considera apenas vendas efetivas: pagamento aprovado e pedido não cancelado.
    // SQL nativo com aliases entre aspas para preservar o nome exato esperado pelas projeções.

    @Query(value = """
            SELECT u.id          AS "unidadeId",
                   u.nome        AS "unidade",
                   u.estado      AS "estado",
                   COUNT(p.id)   AS "quantidadePedidos",
                   SUM(p.total)  AS "faturamento"
            FROM pedido p
            JOIN pagamento pg ON pg.pedido_id = p.id
            JOIN unidade u ON u.id = p.unidade_id
            WHERE pg.status = 'APROVADO'
              AND p.status <> 'CANCELADO'
              AND p.criado_em >= :inicio
              AND p.criado_em < :fimExclusivo
            GROUP BY u.id, u.nome, u.estado
            ORDER BY SUM(p.total) DESC, u.id
            """, nativeQuery = true)
    List<VendaPorUnidadeProjection> somarVendasPorUnidade(@Param("inicio") LocalDateTime inicio,
                                                          @Param("fimExclusivo") LocalDateTime fimExclusivo);

    @Query(value = """
            SELECT pr.id                                  AS "produtoId",
                   pr.nome                                AS "produto",
                   SUM(ip.quantidade)                     AS "quantidadeVendida",
                   SUM(ip.quantidade * ip.preco_unitario) AS "faturamento"
            FROM item_pedido ip
            JOIN pedido p ON p.id = ip.pedido_id
            JOIN pagamento pg ON pg.pedido_id = p.id
            JOIN produto pr ON pr.id = ip.produto_id
            WHERE pg.status = 'APROVADO'
              AND p.status <> 'CANCELADO'
              AND p.criado_em >= :inicio
              AND p.criado_em < :fimExclusivo
            GROUP BY pr.id, pr.nome
            ORDER BY SUM(ip.quantidade) DESC, SUM(ip.quantidade * ip.preco_unitario) DESC, pr.id
            LIMIT 10
            """, nativeQuery = true)
    List<ProdutoVendidoProjection> listarProdutosMaisVendidos(@Param("inicio") LocalDateTime inicio,
                                                              @Param("fimExclusivo") LocalDateTime fimExclusivo);
}
