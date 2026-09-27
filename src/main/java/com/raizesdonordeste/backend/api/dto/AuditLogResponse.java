package com.raizesdonordeste.backend.api.dto;

import com.raizesdonordeste.backend.domain.model.AuditLog;
import java.time.LocalDateTime;

public record AuditLogResponse(Long id, String usuarioEmail, String acao, String entidade,
                                Long entidadeId, String detalhes, LocalDateTime criadoEm) {
    public static AuditLogResponse from(AuditLog log) {
        return new AuditLogResponse(log.getId(), log.getUsuarioEmail(), log.getAcao(),
                log.getEntidade(), log.getEntidadeId(), log.getDetalhes(), log.getCriadoEm());
    }
}