package com.raizesdonordeste.backend.api.controller;

import com.raizesdonordeste.backend.api.dto.ProdutoRequest;
import com.raizesdonordeste.backend.api.dto.ProdutoResponse;
import com.raizesdonordeste.backend.application.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public Page<ProdutoResponse> listar(@RequestParam(defaultValue = "0") int page,
                                         @RequestParam(defaultValue = "10") int limit) {
        return produtoService.listar(PageRequest.of(page, limit)).map(ProdutoResponse::from);
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