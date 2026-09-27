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

    public void registrar(String acao, String entidade, Long entidadeId, String detalhes) {
        String emailUsuario = "sistema";
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            emailUsuario = auth.getName();
        }

        AuditLog log = AuditLog.builder()
                .usuarioEmail(emailUsuario)
                .acao(acao)
                .entidade(entidade)
                .entidadeId(entidadeId)
                .detalhes(detalhes)
                .build();
        auditLogRepository.save(log);
    }

    public Page<AuditLog> consultar(String entidade, Long entidadeId, Pageable pageable) {
        return auditLogRepository.findByEntidadeAndEntidadeId(entidade, entidadeId, pageable);
    }
}