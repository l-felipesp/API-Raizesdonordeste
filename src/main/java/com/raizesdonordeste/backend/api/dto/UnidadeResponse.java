package com.raizesdonordeste.backend.api.dto;

import com.raizesdonordeste.backend.domain.model.Unidade;

public record UnidadeResponse(Long id, String nome, String cidade, String estado, String tipo) {
    public static UnidadeResponse from(Unidade u) {
        return new UnidadeResponse(u.getId(), u.getNome(), u.getCidade(), u.getEstado(), u.getTipo());
    }
}