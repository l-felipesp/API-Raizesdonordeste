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

import com.raizesdonordeste.backend.api.dto.EstoqueRequest;
import com.raizesdonordeste.backend.api.dto.EstoqueResponse;
import com.raizesdonordeste.backend.api.dto.MovimentacaoEstoqueRequest;
import com.raizesdonordeste.backend.api.dto.PaginaResponse;
import com.raizesdonordeste.backend.application.service.EstoqueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@Tag(name = "Estoque", description = "Controle de estoque por unidade")
@RestController
@RequestMapping("/estoque")
public class EstoqueController {

    private static final Set<String> CAMPOS_ORDENACAO = Set.of("id", "quantidade");

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @Operation(summary = "Consultar estoque da unidade",
            description = "Perfis ATENDENTE, GERENTE ou ADMIN. Listagem paginada do saldo por produto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de estoque",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, examples = @ExampleObject(name = "Estoque", value = ExemplosOpenApi.PAGINA_ESTOQUE))),
            @ApiResponse(responseCode = "400", description = "Paginação ou ordenação inválida",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "page inválido", value = ExemplosOpenApi.ERRO_400_PAGINACAO))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Sem token", value = ExemplosOpenApi.ERRO_401))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão para a operação",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Perfil sem permissão", value = ExemplosOpenApi.ERRO_403))),
            @ApiResponse(responseCode = "404", description = "Unidade inexistente",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Não encontrada", value = ExemplosOpenApi.ERRO_404_UNIDADE)))
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE', 'ADMIN')")
    public PaginaResponse<EstoqueResponse> listarPorUnidade(@Parameter(description = "Id da unidade", example = "1") @RequestParam Long unidadeId,
                                                            @Parameter(description = "Página, começando em 1", example = "1") @RequestParam(defaultValue = "1") int page,
                                                            @Parameter(description = "Itens por página, de 1 a 100", example = "10") @RequestParam(defaultValue = "10") int limit,
                                                            @Parameter(description = "Ordenação: campo ou campo,asc|desc. Campos: id, quantidade", example = "quantidade,asc") @RequestParam(required = false) String sort) {
        var pageable = Paginacao.criar(page, limit, sort, CAMPOS_ORDENACAO, Sort.by("id"));
        return PaginaResponse.de(estoqueService.listarPorUnidade(unidadeId, pageable), EstoqueResponse::from);
    }

    @Operation(summary = "Cadastrar estoque de um produto na unidade",
            description = "Perfis GERENTE ou ADMIN. Cada unidade tem no máximo um registro de estoque por produto. Unidades REDUZIDA não aceitam produtos da categoria Pratos Quentes.")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Estoque cadastrado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = EstoqueResponse.class), examples = @ExampleObject(name = "Estoque", value = ExemplosOpenApi.ESTOQUE))),
            @ApiResponse(responseCode = "400", description = "Corpo malformado ou valor de enum inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "JSON inválido", value = ExemplosOpenApi.ERRO_400_REQUISICAO))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Sem token", value = ExemplosOpenApi.ERRO_401))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão para a operação",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Perfil sem permissão", value = ExemplosOpenApi.ERRO_403))),
            @ApiResponse(responseCode = "404", description = "Unidade ou produto inexistente",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404_PRODUTO))),
            @ApiResponse(responseCode = "409", description = "Regra de negócio violada",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = {
                            @ExampleObject(name = "Estoque já existe", value = ExemplosOpenApi.ERRO_409_ESTOQUE_JA_EXISTE),
                            @ExampleObject(name = "Unidade reduzida", value = ExemplosOpenApi.ERRO_409_UNIDADE_SEM_SUPORTE)})),
            @ApiResponse(responseCode = "422", description = "Campos obrigatórios ausentes ou com formato inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Validação", value = ExemplosOpenApi.ERRO_422)))
    })
    @PostMapping
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public ResponseEntity<EstoqueResponse> criar(@Valid @RequestBody EstoqueRequest request) {
        var estoque = estoqueService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(EstoqueResponse.from(estoque));
    }

    @Operation(summary = "Movimentar estoque (entrada ou saída)",
            description = "Perfis ATENDENTE, GERENTE ou ADMIN. ENTRADA soma e SAIDA subtrai; a saída nunca deixa o saldo negativo. Cada movimentação é registrada na auditoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Saldo atualizado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = EstoqueResponse.class), examples = @ExampleObject(name = "Estoque", value = ExemplosOpenApi.ESTOQUE))),
            @ApiResponse(responseCode = "400", description = "Corpo malformado ou valor de enum inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "JSON inválido", value = ExemplosOpenApi.ERRO_400_REQUISICAO))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Sem token", value = ExemplosOpenApi.ERRO_401))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão para a operação",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Perfil sem permissão", value = ExemplosOpenApi.ERRO_403))),
            @ApiResponse(responseCode = "404", description = "Registro de estoque inexistente",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404))),
            @ApiResponse(responseCode = "409", description = "Saldo insuficiente para a saída",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Estoque insuficiente", value = ExemplosOpenApi.ERRO_409_ESTOQUE_MOVIMENTACAO))),
            @ApiResponse(responseCode = "422", description = "Campos obrigatórios ausentes ou com formato inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Validação", value = ExemplosOpenApi.ERRO_422)))
    })
    @PatchMapping("/{id}/movimentar")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE', 'ADMIN')")
    public EstoqueResponse movimentar(@Parameter(description = "Id do registro de estoque", example = "1") @PathVariable Long id, @Valid @RequestBody MovimentacaoEstoqueRequest request) {
        return EstoqueResponse.from(estoqueService.movimentar(id, request));
    }
}
