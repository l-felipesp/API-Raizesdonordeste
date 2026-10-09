package com.raizesdonordeste.backend.api.controller;

import com.raizesdonordeste.backend.api.doc.ExemplosOpenApi;
import com.raizesdonordeste.backend.api.dto.ErroResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;

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

    @Operation(summary = "Consultar auditoria de um registro",
            description = "Perfis GERENTE ou ADMIN. Ações registradas: PEDIDO_CRIADO, PAGAMENTO_PROCESSADO, PAGAMENTO_RETENTADO, STATUS_ATUALIZADO, PEDIDO_CANCELADO, ESTOQUE_MOVIMENTADO, DADOS_PESSOAIS_ACESSADOS, CONSENTIMENTO_CONCEDIDO, CONSENTIMENTO_REVOGADO e USUARIO_ANONIMIZADO.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de registros de auditoria",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, examples = @ExampleObject(name = "Auditoria de um pedido", value = ExemplosOpenApi.PAGINA_AUDITORIA))),
            @ApiResponse(responseCode = "400", description = "Paginação ou ordenação inválida",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "page inválido", value = ExemplosOpenApi.ERRO_400_PAGINACAO))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Sem token", value = ExemplosOpenApi.ERRO_401))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão para a operação",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Perfil sem permissão", value = ExemplosOpenApi.ERRO_403)))
    })
    @GetMapping
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public PaginaResponse<AuditLogResponse> consultar(@Parameter(description = "Tipo do registro auditado: Pedido, Estoque ou Usuario", example = "Pedido") @RequestParam String entidade,
                                                      @Parameter(description = "Id do registro auditado", example = "1") @RequestParam Long entidadeId,
                                                      @Parameter(description = "Página, começando em 1", example = "1") @RequestParam(defaultValue = "1") int page,
                                                      @Parameter(description = "Itens por página, de 1 a 100", example = "10") @RequestParam(defaultValue = "10") int limit,
                                                      @Parameter(description = "Ordenação: campo ou campo,asc|desc. Campos: id, criadoEm", example = "id,desc") @RequestParam(required = false) String sort) {
        var pageable = Paginacao.criar(page, limit, sort, CAMPOS_ORDENACAO, Sort.by(Sort.Direction.DESC, "id"));
        return PaginaResponse.de(auditLogService.consultar(entidade, entidadeId, pageable), AuditLogResponse::from);
    }
}
