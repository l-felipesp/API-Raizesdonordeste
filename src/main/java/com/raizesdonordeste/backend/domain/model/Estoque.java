package com.raizesdonordeste.backend.domain.model;

import jakarta.persistence.*;
import lombok.*;
import com.raizesdonordeste.backend.domain.exception.EstoqueInsuficienteException;

@Entity
@Table(name = "estoque", uniqueConstraints = @UniqueConstraint(columnNames = {"unidade_id", "produto_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Estoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "unidade_id", nullable = false)
    private Unidade unidade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private int quantidade;

    public boolean temDisponibilidade(int quantidadeDesejada) {
        return this.quantidade >= quantidadeDesejada;
    }

    public void debitar(int quantidadeAConsumir) {
        if (!temDisponibilidade(quantidadeAConsumir)) {
            throw new EstoqueInsuficienteException(
                "Estoque insuficiente para o produto " + produto.getNome()
                + ". Disponível: " + this.quantidade);
        }
        this.quantidade -= quantidadeAConsumir;
    }

    public void adicionar(int quantidadeAAdicionar) {
        this.quantidade += quantidadeAAdicionar;
    }

    public void estornar(int quantidadeADevolver) {
        adicionar(quantidadeADevolver);
    }
}