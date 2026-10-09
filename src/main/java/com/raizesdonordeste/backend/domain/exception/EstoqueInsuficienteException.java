package com.raizesdonordeste.backend.domain.exception;

public class EstoqueInsuficienteException extends RegraDeNegocioException {
    public EstoqueInsuficienteException(String mensagem) {
        super("ESTOQUE_INSUFICIENTE", mensagem);
    }

    public EstoqueInsuficienteException(String mensagem, String campo, String problema) {
        super("ESTOQUE_INSUFICIENTE", mensagem, campo, problema);
    }
}
