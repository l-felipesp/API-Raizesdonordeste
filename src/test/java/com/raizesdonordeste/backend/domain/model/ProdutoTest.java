package com.raizesdonordeste.backend.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProdutoTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 6, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 7, 31);

    @Test
    @DisplayName("Produto sem período está sempre disponível")
    void semPeriodo() {
        Produto produto = Produto.builder().build();
        assertTrue(produto.estaDisponivelEm(LocalDate.of(2026, 10, 9)));
    }

    @Test
    @DisplayName("Produto sazonal está disponível dentro do período, incluindo os limites")
    void dentroDoPeriodo() {
        Produto canjica = Produto.builder().disponivelDe(INICIO).disponivelAte(FIM).build();
        assertTrue(canjica.estaDisponivelEm(INICIO));
        assertTrue(canjica.estaDisponivelEm(LocalDate.of(2026, 6, 24)));
        assertTrue(canjica.estaDisponivelEm(FIM));
    }

    @Test
    @DisplayName("Produto sazonal fica indisponível antes e depois do período")
    void foraDoPeriodo() {
        Produto canjica = Produto.builder().disponivelDe(INICIO).disponivelAte(FIM).build();
        assertFalse(canjica.estaDisponivelEm(INICIO.minusDays(1)));
        assertFalse(canjica.estaDisponivelEm(FIM.plusDays(1)));
    }

    @Test
    @DisplayName("Período aberto em uma das pontas")
    void periodoAberto() {
        Produto lancamento = Produto.builder().disponivelDe(INICIO).build();
        Produto descontinuado = Produto.builder().disponivelAte(FIM).build();
        assertFalse(lancamento.estaDisponivelEm(INICIO.minusDays(1)));
        assertTrue(lancamento.estaDisponivelEm(LocalDate.of(2030, 1, 1)));
        assertTrue(descontinuado.estaDisponivelEm(LocalDate.of(2020, 1, 1)));
        assertFalse(descontinuado.estaDisponivelEm(FIM.plusDays(1)));
    }
}
