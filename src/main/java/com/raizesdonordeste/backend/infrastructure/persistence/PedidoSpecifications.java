package com.raizesdonordeste.backend.infrastructure.persistence;

import com.raizesdonordeste.backend.domain.enums.CanalPedido;
import com.raizesdonordeste.backend.domain.enums.StatusPedido;
import com.raizesdonordeste.backend.domain.model.Pedido;
import org.springframework.data.jpa.domain.Specification;

//Filtros opcionais da listagem de pedidos. Cada filtro devolve null quando o parâmetro não foi informado
public final class PedidoSpecifications {

    private PedidoSpecifications() {
    }

    public static Specification<Pedido> doCanal(CanalPedido canalPedido) {
        return (root, query, cb) -> canalPedido == null ? null : cb.equal(root.get("canalPedido"), canalPedido);
    }

    public static Specification<Pedido> comStatus(StatusPedido status) {
        return (root, query, cb) -> status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<Pedido> doCliente(Long clienteId) {
        return (root, query, cb) -> clienteId == null ? null : cb.equal(root.get("cliente").get("id"), clienteId);
    }
}
