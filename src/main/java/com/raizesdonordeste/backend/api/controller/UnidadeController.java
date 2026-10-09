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
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import com.raizesdonordeste.backend.api.dto.*;
import com.raizesdonordeste.backend.application.service.UnidadeService;
import com.raizesdonordeste.backend.domain.model.Unidade;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Set;

@Tag(name = "Unidades", description = "Unidades da rede e cardápio por unidade")
@RestController
@RequestMapping("/unidades")
public class UnidadeController {

    private static final Set<String> CAMPOS_ORDENACAO = Set.of("id", "nome", "cidade", "estado", "tipo");

    private final UnidadeService unidadeService;

    public UnidadeController(UnidadeService unidadeService) {
        this.unidadeService = unidadeService;
    }

    @Operation(summary = "Listar unidades",
            description = "Público. Listagem paginada.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de unidades",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, examples = @ExampleObject(name = "Unidades", value = ExemplosOpenApi.PAGINA_UNIDADES))),
            @ApiResponse(responseCode = "400", description = "Paginação ou ordenação inválida",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "page inválido", value = ExemplosOpenApi.ERRO_400_PAGINACAO)))
    })
    @GetMapping
    public PaginaResponse<UnidadeResponse> listar(@Parameter(description = "Página, começando em 1", example = "1") @RequestParam(defaultValue = "1") int page,
                                                  @Parameter(description = "Itens por página, de 1 a 100", example = "10") @RequestParam(defaultValue = "10") int limit,
                                                  @Parameter(description = "Ordenação: campo ou campo,asc|desc. Campos: id, nome, cidade, estado, tipo", example = "nome,asc") @RequestParam(required = false) String sort) {
        var pageable = Paginacao.criar(page, limit, sort, CAMPOS_ORDENACAO, Sort.by("id"));
        return PaginaResponse.de(unidadeService.listar(pageable), UnidadeResponse::from);
    }

    @Operation(summary = "Consultar unidade",
            description = "Público.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Unidade encontrada",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = UnidadeResponse.class), examples = @ExampleObject(name = "Unidade", value = ExemplosOpenApi.UNIDADE))),
            @ApiResponse(responseCode = "404", description = "Unidade inexistente",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Não encontrada", value = ExemplosOpenApi.ERRO_404_UNIDADE)))
    })
    @GetMapping("/{id}")
    public UnidadeResponse buscar(@Parameter(description = "Id da unidade", example = "1") @PathVariable Long id) {
        return UnidadeResponse.from(unidadeService.buscarPorId(id));
    }

    @Operation(summary = "Consultar cardápio da unidade",
            description = "Público. Produtos com estoque positivo na unidade e dentro do período de disponibilidade: produtos sazonais fora de época não aparecem. Lista curta e limitada a uma unidade, por isso sem paginação.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cardápio da unidade",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, examples = @ExampleObject(name = "Cardápio", value = ExemplosOpenApi.CARDAPIO))),
            @ApiResponse(responseCode = "404", description = "Unidade inexistente",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Não encontrada", value = ExemplosOpenApi.ERRO_404_UNIDADE)))
    })
    // Cardápio não é paginado: é uma lista curta e limitada aos produtos de uma única unidade.
    @GetMapping("/{id}/cardapio")
    public List<ItemCardapioDTO> cardapio(@Parameter(description = "Id da unidade", example = "1") @PathVariable Long id) {
        return unidadeService.consultarCardapio(id);
    }

    @Operation(summary = "Cadastrar unidade",
            description = "Perfis GERENTE ou ADMIN. O tipo deve ser COMPLETA ou REDUZIDA.")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Unidade cadastrada",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = UnidadeResponse.class), examples = @ExampleObject(name = "Unidade", value = ExemplosOpenApi.UNIDADE))),
            @ApiResponse(responseCode = "400", description = "Corpo malformado ou valor de enum inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "JSON inválido", value = ExemplosOpenApi.ERRO_400_REQUISICAO))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Sem token", value = ExemplosOpenApi.ERRO_401))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão para a operação",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Perfil sem permissão", value = ExemplosOpenApi.ERRO_403))),
            @ApiResponse(responseCode = "422", description = "Campos obrigatórios ausentes ou com formato inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Validação", value = ExemplosOpenApi.ERRO_422)))
    })
    @PostMapping
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public ResponseEntity<UnidadeResponse> criar(@Valid @RequestBody UnidadeRequest request) {
        Unidade unidade = Unidade.builder()
                .nome(request.nome()).cidade(request.cidade())
                .estado(request.estado()).tipo(request.tipo())
                .build();
        unidade = unidadeService.criar(unidade);
        return ResponseEntity.status(HttpStatus.CREATED).body(UnidadeResponse.from(unidade));
    }
}
