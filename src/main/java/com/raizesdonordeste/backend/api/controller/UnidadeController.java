package com.raizesdonordeste.backend.api.controller;

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

    @GetMapping
    public PaginaResponse<UnidadeResponse> listar(@RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int limit,
                                                  @RequestParam(required = false) String sort) {
        var pageable = Paginacao.criar(page, limit, sort, CAMPOS_ORDENACAO, Sort.by("id"));
        return PaginaResponse.de(unidadeService.listar(pageable), UnidadeResponse::from);
    }

    @GetMapping("/{id}")
    public UnidadeResponse buscar(@PathVariable Long id) {
        return UnidadeResponse.from(unidadeService.buscarPorId(id));
    }

    // Cardápio não é paginado: é uma lista curta e limitada aos produtos de uma única unidade.
    @GetMapping("/{id}/cardapio")
    public List<ItemCardapioDTO> cardapio(@PathVariable Long id) {
        return unidadeService.consultarCardapio(id);
    }

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
