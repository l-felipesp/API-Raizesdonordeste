package com.raizesdonordeste.backend.application.service;

import com.raizesdonordeste.backend.api.dto.CadastroUsuarioRequest;
import com.raizesdonordeste.backend.domain.enums.Perfil;
import com.raizesdonordeste.backend.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.backend.domain.exception.RegraDeNegocioException;
import com.raizesdonordeste.backend.domain.model.Usuario;
import com.raizesdonordeste.backend.infrastructure.persistence.AuditLogRepository;
import com.raizesdonordeste.backend.infrastructure.persistence.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/** Regras de LGPD do cadastro: consentimento, acesso do titular e anonimização. */
class UsuarioServiceTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 10, 9, 12, 0);

    private UsuarioRepository usuarioRepository;
    private AuditLogRepository auditLogRepository;
    private UsuarioService service;
    private Usuario maria;

    @BeforeEach
    void prepara() {
        usuarioRepository = mock(UsuarioRepository.class);
        auditLogRepository = mock(AuditLogRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(anyString())).thenReturn("hash-bcrypt");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Clock clock = Clock.fixed(Instant.parse("2026-10-09T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        service = new UsuarioService(usuarioRepository, passwordEncoder, new AuditLogService(auditLogRepository), clock);

        maria = Usuario.builder().id(5L).nome("Maria Silva").email("maria@exemplo.com")
                .senhaHash("hash-antigo").perfil(Perfil.CLIENTE).build();
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(maria));
    }

    @Test
    @DisplayName("Cadastro cria CLIENTE com senha em hash e registra o consentimento com data e hora")
    void cadastraComConsentimento() {
        Usuario novo = service.cadastrar(new CadastroUsuarioRequest("Maria Silva", "maria@exemplo.com", "Senha@123", true));

        assertEquals(Perfil.CLIENTE, novo.getPerfil());
        assertEquals("hash-bcrypt", novo.getSenhaHash());
        assertTrue(novo.isConsentimentoFidelizacao());
        assertEquals(AGORA, novo.getConsentimentoFidelizacaoEm());
    }

    @Test
    @DisplayName("Sem resposta sobre fidelização, o cliente fica sem consentimento e sem data registrada")
    void cadastraSemConsentimento() {
        Usuario novo = service.cadastrar(new CadastroUsuarioRequest("Maria Silva", "maria@exemplo.com", "Senha@123", null));

        assertFalse(novo.isConsentimentoFidelizacao());
        assertNull(novo.getConsentimentoFidelizacaoEm());
    }

    @Test
    @DisplayName("Recusa cadastro com e-mail já existente")
    void recusaEmailDuplicado() {
        when(usuarioRepository.existsByEmail("maria@exemplo.com")).thenReturn(true);

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> service.cadastrar(new CadastroUsuarioRequest("Maria", "maria@exemplo.com", "Senha@123", null)));

        assertEquals("EMAIL_JA_CADASTRADO", erro.getCodigoErro());
    }

    @Test
    @DisplayName("Revogação do consentimento é registrada com data e hora")
    void revogaConsentimento() {
        maria.registrarDecisaoFidelizacao(true, AGORA.minusDays(10));

        Usuario atualizado = service.atualizarConsentimento(5L, false);

        assertFalse(atualizado.isConsentimentoFidelizacao());
        assertEquals(AGORA, atualizado.getConsentimentoFidelizacaoEm());
    }

    @Test
    @DisplayName("Anonimização remove os dados pessoais e pseudonimiza o histórico de auditoria")
    void anonimiza() {
        service.anonimizar(5L);

        assertTrue(maria.isAnonimizado());
        assertEquals("Usuário removido", maria.getNome());
        assertEquals("anonimizado-5@removido.invalid", maria.getEmail());
        assertEquals("hash-bcrypt", maria.getSenhaHash());
        verify(auditLogRepository).pseudonimizarResponsavel("maria@exemplo.com", "anonimizado-5@removido.invalid");
    }

    @Test
    @DisplayName("Cadastro anonimizado não pode mais ser consultado")
    void anonimizadoNaoEhEncontrado() {
        service.anonimizar(5L);

        assertThrows(RecursoNaoEncontradoException.class, () -> service.consultarProprioCadastro(5L));
    }
}
