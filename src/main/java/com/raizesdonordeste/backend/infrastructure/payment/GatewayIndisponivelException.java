package com.raizesdonordeste.backend.infrastructure.payment;

public class GatewayIndisponivelException extends RuntimeException {
    public GatewayIndisponivelException(String mensagem) {
        super(mensagem);
    }
}