package com.raizesdonordeste.backend.api.controller;

import com.raizesdonordeste.backend.api.dto.EstoqueRequest;
import com.raizesdonordeste.backend.api.dto.EstoqueResponse;
import com.raizesdonordeste.backend.api.dto.MovimentacaoEstoqueRequest;
import com.raizesdonordeste.backend.application.service.EstoqueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estoque")
public class EstoqueController {

    private final EstoqueService estoqueService;

    public EstoqueController(EstoqueService estoqueService) {
        this.estoqueService = estoqueService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE', 'ADMIN')")
    public List<EstoqueResponse> listarPorUnidade(@RequestParam Long unidadeId) {
        return estoqueService.listarPorUnidade(unidadeId).stream().map(EstoqueResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public ResponseEntity<EstoqueResponse> criar(@Valid @RequestBody EstoqueRequest request) {
        var estoque = estoqueService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(EstoqueResponse.from(estoque));
    }

    @PatchMapping("/{id}/movimentar")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE', 'ADMIN')")
    public EstoqueResponse movimentar(@PathVariable Long id, @Valid @RequestBody MovimentacaoEstoqueRequest request) {
        return EstoqueResponse.from(estoqueService.movimentar(id, request));
    }
}