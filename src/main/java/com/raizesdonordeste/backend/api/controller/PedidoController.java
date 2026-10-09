package com.raizesdonordeste.backend.api.controller;

import com.raizesdonordeste.backend.api.dto.*;
import com.raizesdonordeste.backend.application.service.PedidoService;
import com.raizesdonordeste.backend.domain.enums.CanalPedido;
import com.raizesdonordeste.backend.domain.enums.StatusPedido;
import com.raizesdonordeste.backend.infrastructure.security.UsuarioDetailsImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@Tag(name = "Pedidos", description = "Fluxo crítico: pedido, pagamento mock e status")
@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private static final Set<String> CAMPOS_ORDENACAO = Set.of("id", "criadoEm", "total", "status", "canalPedido");

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ATENDENTE')")
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request,
                                                @AuthenticationPrincipal UsuarioDetailsImpl usuarioLogado) {
        var pedido = pedidoService.criarPedido(request, usuarioLogado.getUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(PedidoResponse.from(pedido));
    }

    @GetMapping
    public PaginaResponse<PedidoResponse> listar(@RequestParam(required = false) CanalPedido canalPedido,
                                                 @RequestParam(required = false) StatusPedido status,
                                                 @RequestParam(defaultValue = "1") int page,
                                                 @RequestParam(defaultValue = "10") int limit,
                                                 @RequestParam(required = false) String sort,
                                                 @AuthenticationPrincipal UsuarioDetailsImpl usuarioLogado) {
        Pageable pageable = Paginacao.criar(page, limit, sort, CAMPOS_ORDENACAO, Sort.by(Sort.Direction.DESC, "id"));
        return PaginaResponse.de(
                pedidoService.listar(canalPedido, status, usuarioLogado.getUsuario(), pageable),
                PedidoResponse::from);
    }

    @GetMapping("/{id}")
    public PedidoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal UsuarioDetailsImpl usuarioLogado) {
        return PedidoResponse.from(pedidoService.buscarParaSolicitante(id, usuarioLogado.getUsuario()));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE', 'ADMIN')")
    public PedidoResponse atualizarStatus(@PathVariable Long id, @Valid @RequestBody AtualizarStatusRequest request) {
        return PedidoResponse.from(pedidoService.atualizarStatus(id, request.novoStatus()));
    }

    @PostMapping("/{id}/pagamento/retentar")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ATENDENTE')")
    public PedidoResponse retentarPagamento(@PathVariable Long id,
                                            @Valid @RequestBody RetentarPagamentoRequest request,
                                            @AuthenticationPrincipal UsuarioDetailsImpl usuarioLogado) {
        return PedidoResponse.from(
                pedidoService.retentarPagamento(id, request.formaPagamento(), usuarioLogado.getUsuario()));
    }
}
