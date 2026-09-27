package com.raizesdonordeste.backend.api.controller;

import com.raizesdonordeste.backend.api.dto.AuditLogResponse;
import com.raizesdonordeste.backend.application.service.AuditLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auditoria", description = "Consulta de logs de ações sensíveis")
@RestController
@RequestMapping("/auditoria")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public Page<AuditLogResponse> consultar(@RequestParam String entidade,
                                             @RequestParam Long entidadeId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int limit) {
        return auditLogService.consultar(entidade, entidadeId, PageRequest.of(page, limit))
                .map(AuditLogResponse::from);
    }
}