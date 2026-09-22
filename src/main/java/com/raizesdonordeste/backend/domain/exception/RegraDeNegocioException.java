package com.raizesdonordeste.backend.domain.exception;

public class RegraDeNegocioException extends RuntimeException {
    private final String codigoErro;

    public RegraDeNegocioException(String codigoErro, String mensagem) {
        super(mensagem);
        this.codigoErro = codigoErro;
    }

    public String getCodigoErro() {
        return codigoErro;
    }
}