package com.raizesdonordeste.backend.domain.exception;

public class RegraDeNegocioException extends RuntimeException {
    private final String codigoErro;
    private final String campo;
    private final String problema;

    public RegraDeNegocioException(String codigoErro, String mensagem) {
        this(codigoErro, mensagem, null, null);
    }

    /**
     * @param campo    campo do request relacionado à violação (ex.: "itens[0].quantidade"), ou null
     * @param problema descrição curta do problema nesse campo, ou null
     */
    public RegraDeNegocioException(String codigoErro, String mensagem, String campo, String problema) {
        super(mensagem);
        this.codigoErro = codigoErro;
        this.campo = campo;
        this.problema = problema;
    }

    public String getCodigoErro() {
        return codigoErro;
    }

    public String getCampo() {
        return campo;
    }

    public String getProblema() {
        return problema;
    }
}
