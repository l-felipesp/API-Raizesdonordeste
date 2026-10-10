package com.raizesdonordeste.backend.domain.model;

import com.raizesdonordeste.backend.domain.enums.StatusPedido;
import com.raizesdonordeste.backend.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PedidoTest {

    private ItemPedido item(String preco, int quantidade) {
        return ItemPedido.builder().precoUnitario(new BigDecimal(preco)).quantidade(quantidade).build();
    }

    @Test
    @DisplayName("Total é a soma dos subtotais dos itens")
    void calculaTotal() {
        Pedido pedido = Pedido.builder().build();
        pedido.adicionarItem(item("12.50", 2));
        pedido.adicionarItem(item("8.50", 1));

        pedido.calcularTotal();

        assertEquals(0, new BigDecimal("33.50").compareTo(pedido.getTotal()));
        assertEquals(pedido, pedido.getItens().get(0).getPedido());
    }

    @Test
    @DisplayName("Atualiza o status quando a transição é permitida")
    void atualizaStatusValido() {
        Pedido pedido = Pedido.builder().status(StatusPedido.EM_PREPARO).build();

        pedido.atualizarStatus(StatusPedido.PRONTO);

        assertEquals(StatusPedido.PRONTO, pedido.getStatus());
    }

    @Test
    @DisplayName("Recusa transição inválida e mantém o status atual")
    void recusaTransicaoInvalida() {
        Pedido pedido = Pedido.builder().status(StatusPedido.EM_PREPARO).build();

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> pedido.atualizarStatus(StatusPedido.ENTREGUE));

        assertEquals("TRANSICAO_INVALIDA", erro.getCodigoErro());
        assertEquals(StatusPedido.EM_PREPARO, pedido.getStatus());
    }
}
