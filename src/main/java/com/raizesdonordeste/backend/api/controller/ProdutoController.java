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

import com.raizesdonordeste.backend.api.dto.PaginaResponse;
import com.raizesdonordeste.backend.api.dto.ProdutoRequest;
import com.raizesdonordeste.backend.api.dto.ProdutoResponse;
import com.raizesdonordeste.backend.application.service.ProdutoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@Tag(name = "Produtos", description = "Cadastro de produtos")
@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private static final Set<String> CAMPOS_ORDENACAO = Set.of("id", "nome", "preco", "categoria");

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @Operation(summary = "Listar produtos",
            description = "Público. Listagem paginada de todos os produtos da rede.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de produtos",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, examples = @ExampleObject(name = "Produtos", value = ExemplosOpenApi.PAGINA_PRODUTOS))),
            @ApiResponse(responseCode = "400", description = "Paginação ou ordenação inválida",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "page inválido", value = ExemplosOpenApi.ERRO_400_PAGINACAO)))
    })
    @GetMapping
    public PaginaResponse<ProdutoResponse> listar(@Parameter(description = "Página, começando em 1", example = "1") @RequestParam(defaultValue = "1") int page,
                                                  @Parameter(description = "Itens por página, de 1 a 100", example = "10") @RequestParam(defaultValue = "10") int limit,
                                                  @Parameter(description = "Ordenação: campo ou campo,asc|desc. Campos: id, nome, preco, categoria", example = "preco,desc") @RequestParam(required = false) String sort) {
        var pageable = Paginacao.criar(page, limit, sort, CAMPOS_ORDENACAO, Sort.by("id"));
        return PaginaResponse.de(produtoService.listar(pageable), ProdutoResponse::from);
    }

    @Operation(summary = "Consultar produto",
            description = "Público.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto encontrado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ProdutoResponse.class), examples = {
                            @ExampleObject(name = "Produto", value = ExemplosOpenApi.PRODUTO),
                            @ExampleObject(name = "Produto sazonal", value = ExemplosOpenApi.PRODUTO_SAZONAL)})),
            @ApiResponse(responseCode = "404", description = "Produto inexistente",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404_PRODUTO)))
    })
    @GetMapping("/{id}")
    public ProdutoResponse buscar(@Parameter(description = "Id do produto", example = "1") @PathVariable Long id) {
        return ProdutoResponse.from(produtoService.buscarPorId(id));
    }

    @Operation(summary = "Cadastrar produto",
            description = "Perfis GERENTE ou ADMIN. disponivelDe e disponivelAte (yyyy-MM-dd) são opcionais e definem o período de produtos sazonais.")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produto cadastrado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ProdutoResponse.class), examples = {
                            @ExampleObject(name = "Produto", value = ExemplosOpenApi.PRODUTO),
                            @ExampleObject(name = "Produto sazonal", value = ExemplosOpenApi.PRODUTO_SAZONAL)})),
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
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request) {
        var produto = produtoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProdutoResponse.from(produto));
    }

    @Operation(summary = "Atualizar produto",
            description = "Perfis GERENTE ou ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produto atualizado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ProdutoResponse.class), examples = @ExampleObject(name = "Produto", value = ExemplosOpenApi.PRODUTO))),
            @ApiResponse(responseCode = "400", description = "Corpo malformado ou valor de enum inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "JSON inválido", value = ExemplosOpenApi.ERRO_400_REQUISICAO))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Sem token", value = ExemplosOpenApi.ERRO_401))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão para a operação",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Perfil sem permissão", value = ExemplosOpenApi.ERRO_403))),
            @ApiResponse(responseCode = "404", description = "Produto inexistente",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404_PRODUTO))),
            @ApiResponse(responseCode = "422", description = "Campos obrigatórios ausentes ou com formato inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Validação", value = ExemplosOpenApi.ERRO_422)))
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public ProdutoResponse atualizar(@Parameter(description = "Id do produto", example = "1") @PathVariable Long id, @Valid @RequestBody ProdutoRequest request) {
        return ProdutoResponse.from(produtoService.atualizar(id, request));
    }
}
