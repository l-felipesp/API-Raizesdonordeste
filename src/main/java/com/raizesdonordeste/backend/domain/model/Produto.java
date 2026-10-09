package com.raizesdonordeste.backend.domain.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "produto")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    private String categoria;

    // Produtos sazonais
    @Column(name = "disponivel_de")
    private LocalDate disponivelDe;

    @Column(name = "disponivel_ate")
    private LocalDate disponivelAte;

    public boolean estaDisponivelEm(LocalDate data) {
        boolean jaComecou = disponivelDe == null || !data.isBefore(disponivelDe);
        boolean aindaNaoTerminou = disponivelAte == null || !data.isAfter(disponivelAte);
        return jaComecou && aindaNaoTerminou;
    }
}