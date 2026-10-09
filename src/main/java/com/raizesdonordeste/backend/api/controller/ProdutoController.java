package com.raizesdonordeste.backend.api.controller;

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

    @GetMapping
    public PaginaResponse<ProdutoResponse> listar(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int limit,
                                                  @RequestParam(required = false) String sort) {
        var pageable = Paginacao.criar(page, limit, sort, CAMPOS_ORDENACAO, Sort.by("id"));
        return PaginaResponse.de(produtoService.listar(pageable), ProdutoResponse::from);
    }

    @GetMapping("/{id}")
    public ProdutoResponse buscar(@PathVariable Long id) {
        return ProdutoResponse.from(produtoService.buscarPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public ResponseEntity<ProdutoResponse> criar(@Valid @RequestBody ProdutoRequest request) {
        var produto = produtoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProdutoResponse.from(produto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public ProdutoResponse atualizar(@PathVariable Long id, @Valid @RequestBody ProdutoRequest request) {
        return ProdutoResponse.from(produtoService.atualizar(id, request));
    }
}
