package com.raizesdonordeste.backend.api.controller;

import com.raizesdonordeste.backend.api.dto.*;
import com.raizesdonordeste.backend.application.service.PedidoService;
import com.raizesdonordeste.backend.domain.enums.CanalPedido;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ATENDENTE')")
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request, Authentication auth) {
        var pedido = pedidoService.criarPedido(request, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(PedidoResponse.from(pedido));
    }

    @GetMapping
    public Page<PedidoResponse> listar(@RequestParam(required = false) CanalPedido canalPedido,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int limit) {
        return pedidoService.listar(canalPedido, PageRequest.of(page, limit)).map(PedidoResponse::from);
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable Long id) {
        return PedidoResponse.from(pedidoService.buscarPorId(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE', 'ADMIN')")
    public PedidoResponse atualizarStatus(@PathVariable Long id, @Valid @RequestBody AtualizarStatusRequest request) {
        return PedidoResponse.from(pedidoService.atualizarStatus(id, request.novoStatus()));
    }
}