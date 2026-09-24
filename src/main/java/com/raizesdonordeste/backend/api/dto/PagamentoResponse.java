package com.raizesdonordeste.backend.api.dto;

import com.raizesdonordeste.backend.domain.model.Pagamento;

public record PagamentoResponse(String status, String formaPagamento) {
    public static PagamentoResponse from(Pagamento p) {
        return new PagamentoResponse(p.getStatus().name(), p.getFormaPagamento());
    }
}