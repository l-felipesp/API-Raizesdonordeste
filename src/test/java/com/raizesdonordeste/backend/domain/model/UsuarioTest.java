package com.raizesdonordeste.backend.domain.model;

import com.raizesdonordeste.backend.domain.enums.Perfil;
import com.raizesdonordeste.backend.domain.exception.RegraDeNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 9, 12, 0);

    private Usuario usuario;

    @BeforeEach
    void prepara() {
        usuario = Usuario.builder().id(5L).nome("Maria Silva").email("maria@exemplo.com")
                .senhaHash("hash-original").perfil(Perfil.CLIENTE).build();
        usuario.registrarDecisaoFidelizacao(true, AGORA.minusDays(1));
    }

    @Test
    @DisplayName("Registra a decisão de consentimento com data e hora")
    void registraConsentimento() {
        usuario.registrarDecisaoFidelizacao(false, AGORA);

        assertFalse(usuario.isConsentimentoFidelizacao());
        assertEquals(AGORA, usuario.getConsentimentoFidelizacaoEm());
    }

    @Test
    @DisplayName("Anonimização remove os dados pessoais e revoga o consentimento")
    void anonimiza() {
        usuario.anonimizar("hash-inutilizavel", AGORA);

        assertEquals("Usuário removido", usuario.getNome());
        assertEquals("anonimizado-5@removido.invalid", usuario.getEmail());
        assertEquals("hash-inutilizavel", usuario.getSenhaHash());
        assertFalse(usuario.isConsentimentoFidelizacao());
        assertTrue(usuario.isAnonimizado());
        assertEquals(AGORA, usuario.getAnonimizadoEm());
    }

    @Test
    @DisplayName("Não permite anonimizar o mesmo cadastro duas vezes")
    void naoAnonimizaDuasVezes() {
        usuario.anonimizar("hash-inutilizavel", AGORA);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> usuario.anonimizar("outro-hash", AGORA.plusHours(1)));

        assertEquals("USUARIO_JA_ANONIMIZADO", erro.getCodigoErro());
    }
}
