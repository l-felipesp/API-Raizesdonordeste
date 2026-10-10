package com.raizesdonordeste.backend.domain.model;

import com.raizesdonordeste.backend.domain.exception.EstoqueInsuficienteException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EstoqueTest {

    private Estoque estoque;

    @BeforeEach
    void prepara() {
        Produto tapioca = Produto.builder().id(1L).nome("Tapioca de Queijo Coalho").build();
        estoque = Estoque.builder().produto(tapioca).quantidade(10).build();
    }

    @Test
    @DisplayName("Saída reduz o saldo")
    void debitaSaldo() {
        estoque.debitar(3);
        assertEquals(7, estoque.getQuantidade());
    }

    @Test
    @DisplayName("Saída igual ao saldo zera o estoque")
    void debitaSaldoInteiro() {
        estoque.debitar(10);
        assertEquals(0, estoque.getQuantidade());
    }

    @Test
    @DisplayName("Saída maior que o saldo é recusada e o saldo não muda")
    void recusaSaidaMaiorQueSaldo() {
        EstoqueInsuficienteException erro = assertThrows(EstoqueInsuficienteException.class, () -> estoque.debitar(11));

        assertEquals("ESTOQUE_INSUFICIENTE", erro.getCodigoErro());
        assertEquals(10, estoque.getQuantidade());
    }

    @Test
    @DisplayName("Estorno devolve a quantidade ao saldo")
    void estornaQuantidade() {
        estoque.debitar(4);
        estoque.estornar(4);
        assertEquals(10, estoque.getQuantidade());
    }
}
