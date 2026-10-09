package com.raizesdonordeste.backend.api.exception;

public class ParametroInvalidoException extends RuntimeException {
    private final String campo;

    public ParametroInvalidoException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
