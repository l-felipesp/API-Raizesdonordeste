package com.raizesdonordeste.backend.domain.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "unidade")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Unidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String cidade;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private String tipo; // "COMPLETA" ou "REDUZIDA" — string simples aqui é suficiente, não é regra crítica
}