package com.raizesdonordeste.backend.application.service;

import com.raizesdonordeste.backend.api.dto.CadastroUsuarioRequest;
import com.raizesdonordeste.backend.domain.enums.Perfil;
import com.raizesdonordeste.backend.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.backend.domain.exception.RegraDeNegocioException;
import com.raizesdonordeste.backend.domain.model.Usuario;
import com.raizesdonordeste.backend.infrastructure.persistence.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                          AuditLogService auditLogService, Clock clock) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional
    public Usuario cadastrar(CadastroUsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new RegraDeNegocioException("EMAIL_JA_CADASTRADO",
                    "Já existe um usuário cadastrado com este e-mail.", "email", "E-mail já cadastrado");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .perfil(Perfil.CLIENTE)
                .build();

        // O consentimento só é registrado (com data e hora) se o titular respondeu.
        if (request.aceitaFidelizacao() != null) {
            usuario.registrarDecisaoFidelizacao(request.aceitaFidelizacao(), LocalDateTime.now(clock));
        }
        return usuarioRepository.save(usuario);
    }

    // LGPD: acesso do titular aos próprios dados, registrado na auditoria.
    @Transactional
    public Usuario consultarProprioCadastro(Long usuarioId) {
        Usuario usuario = buscarAtivo(usuarioId);
        auditLogService.registrar("DADOS_PESSOAIS_ACESSADOS", "Usuario", usuario.getId(), "acesso pelo próprio titular");
        return usuario;
    }

    //LGPD: concessão ou revogação do consentimento para fidelização.
    @Transactional
    public Usuario atualizarConsentimento(Long usuarioId, boolean aceita) {
        Usuario usuario = buscarAtivo(usuarioId);
        usuario.registrarDecisaoFidelizacao(aceita, LocalDateTime.now(clock));
        auditLogService.registrar(aceita ? "CONSENTIMENTO_CONCEDIDO" : "CONSENTIMENTO_REVOGADO",
                "Usuario", usuario.getId(), "finalidade=fidelizacao");
        return usuarioRepository.save(usuario);
    }

    //LGPD: exclusão dos dados pessoais a pedido do titular.
    //O cadastro é anonimizado em vez de apagado, porque os pedidos ligados a ele precisam ser mantidos (obrigação fiscal e relatórios).
    @Transactional
    public void anonimizar(Long usuarioId) {
        Usuario usuario = buscarAtivo(usuarioId);
        String emailOriginal = usuario.getEmail();

        usuario.anonimizar(passwordEncoder.encode(UUID.randomUUID().toString()), LocalDateTime.now(clock));
        usuarioRepository.save(usuario);

        auditLogService.pseudonimizarResponsavel(emailOriginal, usuario.getEmail());
        auditLogService.registrarComo(usuario.getEmail(), "USUARIO_ANONIMIZADO", "Usuario", usuario.getId(),
                "dados pessoais removidos a pedido do titular");
    }

    private Usuario buscarAtivo(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado: id " + usuarioId));
        if (usuario.isAnonimizado()) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado: id " + usuarioId);
        }
        return usuario;
    }
}
