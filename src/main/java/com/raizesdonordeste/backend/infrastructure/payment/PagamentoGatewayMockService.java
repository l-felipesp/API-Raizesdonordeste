package com.raizesdonordeste.backend.infrastructure.payment;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class PagamentoGatewayMockService {

    private static final BigDecimal LIMITE_APROVACAO = new BigDecimal("500.00");
    private static final String FORMA_PAGAMENTO_SIMULA_FALHA = "PAGAMENTO_INDISPONIVEL";

    public ResultadoPagamentoMock processar(BigDecimal valor, String formaPagamento) {
        if (FORMA_PAGAMENTO_SIMULA_FALHA.equalsIgnoreCase(formaPagamento)) {
            throw new GatewayIndisponivelException(
                    "Gateway de pagamento mock indisponível (cenário simulado de falha de comunicação).");
        }
        boolean aprovado = valor.compareTo(LIMITE_APROVACAO) < 0;
        String motivo = aprovado ? "APROVADO_MOCK" : "RECUSADO_LIMITE_MOCK";
        return new ResultadoPagamentoMock(aprovado, motivo);
    }
}