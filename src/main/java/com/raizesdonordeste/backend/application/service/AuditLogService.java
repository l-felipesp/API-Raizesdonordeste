package com.raizesdonordeste.backend.application.service;

import com.raizesdonordeste.backend.domain.model.AuditLog;
import com.raizesdonordeste.backend.infrastructure.persistence.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // Registra a ação em nome do usuário autenticado na requisição atual.
    public void registrar(String acao, String entidade, Long entidadeId, String detalhes) {
        String emailUsuario = "sistema";
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            emailUsuario = auth.getName();
        }
        registrarComo(emailUsuario, acao, entidade, entidadeId, detalhes);
    }

    // Registra a ação informando explicitamente o responsável (usado quando ele não é o usuário do token)
    public void registrarComo(String responsavel, String acao, String entidade, Long entidadeId, String detalhes) {
        AuditLog log = AuditLog.builder()
                .usuarioEmail(responsavel)
                .acao(acao)
                .entidade(entidade)
                .entidadeId(entidadeId)
                .detalhes(detalhes)
                .build();
        auditLogRepository.save(log);
    }

    // LGPD: troca o e-mail do responsável por um pseudônimo em todo o histórico.
    public int pseudonimizarResponsavel(String emailOriginal, String pseudonimo) {
        return auditLogRepository.pseudonimizarResponsavel(emailOriginal, pseudonimo);
    }

    public Page<AuditLog> consultar(String entidade, Long entidadeId, Pageable pageable) {
        return auditLogRepository.findByEntidadeAndEntidadeId(entidade, entidadeId, pageable);
    }
}
