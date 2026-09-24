package com.raizesdonordeste.backend.infrastructure.payment;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class PagamentoGatewayMockService {

    private static final BigDecimal LIMITE_APROVACAO = new BigDecimal("500.00");

    public ResultadoPagamentoMock processar(BigDecimal valor, String formaPagamento) {
        boolean aprovado = valor.compareTo(LIMITE_APROVACAO) < 0;
        String motivo = aprovado ? "APROVADO_MOCK" : "RECUSADO_LIMITE_MOCK";
        return new ResultadoPagamentoMock(aprovado, motivo);
    }
}