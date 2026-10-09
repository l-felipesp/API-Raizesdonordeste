package com.raizesdonordeste.backend.domain.exception;

// Regra de autorização que depende dos dados
public class AcessoNegadoException extends RuntimeException {
    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
