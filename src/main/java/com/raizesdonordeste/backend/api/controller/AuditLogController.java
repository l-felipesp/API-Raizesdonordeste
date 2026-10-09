package com.raizesdonordeste.backend.api.controller;

import com.raizesdonordeste.backend.api.dto.AuditLogResponse;
import com.raizesdonordeste.backend.api.dto.PaginaResponse;
import com.raizesdonordeste.backend.application.service.AuditLogService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@Tag(name = "Auditoria", description = "Consulta de logs de ações sensíveis")
@RestController
@RequestMapping("/auditoria")
public class AuditLogController {

    private static final Set<String> CAMPOS_ORDENACAO = Set.of("id", "criadoEm");

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public PaginaResponse<AuditLogResponse> consultar(@RequestParam String entidade,
                                                      @RequestParam Long entidadeId,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int limit,
                                                      @RequestParam(required = false) String sort) {
        var pageable = Paginacao.criar(page, limit, sort, CAMPOS_ORDENACAO, Sort.by(Sort.Direction.DESC, "id"));
        return PaginaResponse.de(auditLogService.consultar(entidade, entidadeId, pageable), AuditLogResponse::from);
    }
}
