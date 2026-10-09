package com.raizesdonordeste.backend.api.dto;

import com.raizesdonordeste.backend.domain.model.Usuario;
import java.time.LocalDateTime;

public record UsuarioDetalheResponse(Long id, String nome, String email, String perfil, LocalDateTime criadoEm,
                                     boolean consentimentoFidelizacao, LocalDateTime consentimentoFidelizacaoEm) {
    public static UsuarioDetalheResponse from(Usuario u) {
        return new UsuarioDetalheResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil().name(), u.getCriadoEm(),
                u.isConsentimentoFidelizacao(), u.getConsentimentoFidelizacaoEm());
    }
}
