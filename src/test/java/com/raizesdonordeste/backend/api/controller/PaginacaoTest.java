package com.raizesdonordeste.backend.api.controller;

import com.raizesdonordeste.backend.api.exception.ParametroInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PaginacaoTest {

    private static final Set<String> CAMPOS = Set.of("id", "nome", "preco");
    private static final Sort PADRAO = Sort.by("id");

    @Test
    @DisplayName("A página 1 da API corresponde à primeira página do Spring")
    void converteNumeracaoDaPagina() {
        Pageable pageable = Paginacao.criar(1, 10, null, CAMPOS, PADRAO);

        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(PADRAO, pageable.getSort());
    }

    @Test
    @DisplayName("Aplica a ordenação informada")
    void aplicaOrdenacao() {
        Pageable pageable = Paginacao.criar(2, 5, "preco,desc", CAMPOS, PADRAO);

        assertEquals(1, pageable.getPageNumber());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("preco").getDirection());
    }

    @Test
    @DisplayName("Recusa página menor que 1 e limite fora do intervalo")
    void recusaPaginaELimiteInvalidos() {
        assertEquals("page", assertThrows(ParametroInvalidoException.class,
                () -> Paginacao.criar(0, 10, null, CAMPOS, PADRAO)).getCampo());
        assertEquals("limit", assertThrows(ParametroInvalidoException.class,
                () -> Paginacao.criar(1, 101, null, CAMPOS, PADRAO)).getCampo());
        assertEquals("limit", assertThrows(ParametroInvalidoException.class,
                () -> Paginacao.criar(1, 0, null, CAMPOS, PADRAO)).getCampo());
    }

    @Test
    @DisplayName("Recusa ordenação por campo não permitido ou direção inválida")
    void recusaOrdenacaoInvalida() {
        assertEquals("sort", assertThrows(ParametroInvalidoException.class,
                () -> Paginacao.criar(1, 10, "senhaHash", CAMPOS, PADRAO)).getCampo());
        assertEquals("sort", assertThrows(ParametroInvalidoException.class,
                () -> Paginacao.criar(1, 10, "nome,para-cima", CAMPOS, PADRAO)).getCampo());
    }
}
