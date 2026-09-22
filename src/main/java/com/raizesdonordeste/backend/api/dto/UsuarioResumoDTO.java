package com.raizesdonordeste.backend.api.dto;

import com.raizesdonordeste.backend.domain.model.Usuario;

public record UsuarioResumoDTO(Long id, String nome, String perfil) {
    public static UsuarioResumoDTO from(Usuario usuario) {
        return new UsuarioResumoDTO(usuario.getId(), usuario.getNome(), usuario.getPerfil().name());
    }
}