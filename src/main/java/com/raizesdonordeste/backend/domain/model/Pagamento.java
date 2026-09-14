package com.raizesdonordeste.backend.domain.model;

import com.raizesdonordeste.backend.domain.enums.StatusPagamento;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamento")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPagamento status;

    @Column(name = "forma_pagamento", nullable = false)
    private String formaPagamento;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
        if (this.status == null) {
            this.status = StatusPagamento.PENDENTE;
        }
    }

    public void aprovar() { this.status = StatusPagamento.APROVADO; }
    public void recusar() { this.status = StatusPagamento.RECUSADO; }
}