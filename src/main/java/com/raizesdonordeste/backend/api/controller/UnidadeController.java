package com.raizesdonordeste.backend.api.controller;

import com.raizesdonordeste.backend.api.dto.*;
import com.raizesdonordeste.backend.application.service.UnidadeService;
import com.raizesdonordeste.backend.domain.model.Unidade;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/unidades")
public class UnidadeController {

    private final UnidadeService unidadeService;

    public UnidadeController(UnidadeService unidadeService) {
        this.unidadeService = unidadeService;
    }

    @GetMapping
    public List<UnidadeResponse> listar() {
        return unidadeService.listarTodas().stream().map(UnidadeResponse::from).toList();
    }

    @GetMapping("/{id}")
    public UnidadeResponse buscar(@PathVariable Long id) {
        return UnidadeResponse.from(unidadeService.buscarPorId(id));
    }

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