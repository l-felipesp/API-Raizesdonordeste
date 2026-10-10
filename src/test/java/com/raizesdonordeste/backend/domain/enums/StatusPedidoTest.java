package com.raizesdonordeste.backend.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.raizesdonordeste.backend.domain.enums.StatusPedido.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusPedidoTest {

    @Test
    @DisplayName("Permite as transições previstas no fluxo do pedido")
    void permiteTransicoesDoFluxo() {
        assertTrue(AGUARDANDO_PAGAMENTO.podeTransicionarPara(EM_PREPARO));
        assertTrue(AGUARDANDO_PAGAMENTO.podeTransicionarPara(PAGAMENTO_RECUSADO));
        assertTrue(AGUARDANDO_PAGAMENTO.podeTransicionarPara(CANCELADO));
        assertTrue(EM_PREPARO.podeTransicionarPara(PRONTO));
        assertTrue(EM_PREPARO.podeTransicionarPara(CANCELADO));
        assertTrue(PRONTO.podeTransicionarPara(ENTREGUE));
        assertTrue(PRONTO.podeTransicionarPara(CANCELADO));
    }

    @Test
    @DisplayName("Bloqueia saltos de etapa e retrocessos")
    void bloqueiaSaltosERetrocessos() {
        assertFalse(AGUARDANDO_PAGAMENTO.podeTransicionarPara(PRONTO));
        assertFalse(AGUARDANDO_PAGAMENTO.podeTransicionarPara(ENTREGUE));
        assertFalse(EM_PREPARO.podeTransicionarPara(ENTREGUE));
        assertFalse(PRONTO.podeTransicionarPara(EM_PREPARO));
        assertFalse(EM_PREPARO.podeTransicionarPara(AGUARDANDO_PAGAMENTO));
    }

    @Test
    @DisplayName("Entregue, cancelado e pagamento recusado são estados finais")
    void estadosFinaisNaoTemSaida() {
        for (StatusPedido destino : StatusPedido.values()) {
            assertFalse(ENTREGUE.podeTransicionarPara(destino));
            assertFalse(CANCELADO.podeTransicionarPara(destino));
            assertFalse(PAGAMENTO_RECUSADO.podeTransicionarPara(destino));
        }
    }
}
