package com.raizesdonordeste.backend.api.controller;

import com.raizesdonordeste.backend.api.exception.ParametroInvalidoException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.Set;
import java.util.TreeSet;

//Converte os parâmetros page/limit/sort das listagens em um Pageable.
//page começa em 1; limit vai de 1 a 100; sort aceita "campo" ou "campo,asc|desc", apenas entre os campos permitidos de cada recurso.
public final class Paginacao {

    public static final int LIMITE_MAXIMO = 100;

    private Paginacao() {
    }

    public static Pageable criar(int page, int limit, String sort, Set<String> camposPermitidos, Sort ordenacaoPadrao) {
        if (page < 1) {
            throw new ParametroInvalidoException("page", "A página deve ser maior ou igual a 1.");
        }
        if (limit < 1 || limit > LIMITE_MAXIMO) {
            throw new ParametroInvalidoException("limit", "O limite deve estar entre 1 e " + LIMITE_MAXIMO + ".");
        }
        return PageRequest.of(page - 1, limit, interpretarOrdenacao(sort, camposPermitidos, ordenacaoPadrao));
    }

    private static Sort interpretarOrdenacao(String sort, Set<String> camposPermitidos, Sort ordenacaoPadrao) {
        if (sort == null || sort.isBlank()) {
            return ordenacaoPadrao;
        }
        String[] partes = sort.split(",");
        String campo = partes[0].trim();
        if (partes.length > 2 || !camposPermitidos.contains(campo)) {
            throw new ParametroInvalidoException("sort",
                    "Ordenação inválida. Use campo ou campo,asc|desc. Campos permitidos: "
                            + String.join(", ", new TreeSet<>(camposPermitidos)) + ".");
        }
        Sort.Direction direcao = Sort.Direction.ASC;
        if (partes.length == 2) {
            String valor = partes[1].trim();
            if (valor.equalsIgnoreCase("desc")) {
                direcao = Sort.Direction.DESC;
            } else if (!valor.equalsIgnoreCase("asc")) {
                throw new ParametroInvalidoException("sort", "A direção da ordenação deve ser asc ou desc.");
            }
        }
        return Sort.by(direcao, campo);
    }
}
