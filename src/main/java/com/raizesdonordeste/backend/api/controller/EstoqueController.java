package com.raizesdonordeste.backend.api.controller;

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

    @GetMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE', 'ADMIN')")
    public PaginaResponse<EstoqueResponse> listarPorUnidade(@RequestParam Long unidadeId,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int limit,
                                                            @RequestParam(required = false) String sort) {
        var pageable = Paginacao.criar(page, limit, sort, CAMPOS_ORDENACAO, Sort.by("id"));
        return PaginaResponse.de(estoqueService.listarPorUnidade(unidadeId, pageable), EstoqueResponse::from);
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
