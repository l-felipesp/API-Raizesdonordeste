package com.raizesdonordeste.backend.infrastructure.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class PagamentoGatewayMockServiceTest {

    private final PagamentoGatewayMockService gateway = new PagamentoGatewayMockService();

    @Test
    @DisplayName("Aprova pagamentos abaixo de R$ 500,00")
    void aprovaAbaixoDoLimite() {
        assertTrue(gateway.processar(new BigDecimal("499.99"), "PIX").aprovado());
    }

    @Test
    @DisplayName("Recusa pagamentos a partir de R$ 500,00")
    void recusaAPartirDoLimite() {
        assertFalse(gateway.processar(new BigDecimal("500.00"), "PIX").aprovado());
        assertFalse(gateway.processar(new BigDecimal("562.50"), "CARTAO").aprovado());
    }

    @Test
    @DisplayName("Simula falha de comunicação com a forma de pagamento PAGAMENTO_INDISPONIVEL")
    void simulaFalhaDeComunicacao() {
        assertThrows(GatewayIndisponivelException.class,
                () -> gateway.processar(new BigDecimal("10.00"), "PAGAMENTO_INDISPONIVEL"));
        assertThrows(GatewayIndisponivelException.class,
                () -> gateway.processar(new BigDecimal("10.00"), "pagamento_indisponivel"));
    }
}
