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

    @Operation(summary = "Criar pedido (fluxo crítico)",
            description = "Perfis CLIENTE ou ATENDENTE. Valida itens, estoque e período de disponibilidade, calcula o total no servidor, reserva o estoque e solicita o pagamento ao gateway mock. Regras do mock: total a partir de R$ 500,00 é recusado (estoque devolvido, status PAGAMENTO_RECUSADO); formaPagamento PAGAMENTO_INDISPONIVEL simula falha de comunicação (status AGUARDANDO_PAGAMENTO, permite nova tentativa); nos demais casos o pagamento é aprovado (status EM_PREPARO). clienteId é opcional e serve para o ATENDENTE registrar um pedido de balcão em nome de um cliente.")
    @ResponseStatus(HttpStatus.CREATED)
    @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
            content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = PedidoRequest.class), examples = {
                            @ExampleObject(name = "App - pagamento aprovado", value = ExemplosOpenApi.REQ_PEDIDO_APP),
                            @ExampleObject(name = "Totem - pagamento recusado (total >= R$ 500)", value = ExemplosOpenApi.REQ_PEDIDO_RECUSADO),
                            @ExampleObject(name = "Web - falha de comunicação com o gateway", value = ExemplosOpenApi.REQ_PEDIDO_GATEWAY_FORA),
                            @ExampleObject(name = "Balcão - atendente em nome de um cliente", value = ExemplosOpenApi.REQ_PEDIDO_BALCAO)}))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Pedido criado; o status reflete o retorno do pagamento",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = PedidoResponse.class), examples = {
                            @ExampleObject(name = "Pagamento aprovado", value = ExemplosOpenApi.PEDIDO_APROVADO),
                            @ExampleObject(name = "Pagamento recusado", value = ExemplosOpenApi.PEDIDO_RECUSADO),
                            @ExampleObject(name = "Gateway indisponível", value = ExemplosOpenApi.PEDIDO_PENDENTE)})),
            @ApiResponse(responseCode = "400", description = "Corpo malformado ou valor de enum inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "JSON inválido", value = ExemplosOpenApi.ERRO_400_REQUISICAO))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Sem token", value = ExemplosOpenApi.ERRO_401))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão ou cliente pedindo em nome de outro",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = {
                            @ExampleObject(name = "Perfil sem permissão", value = ExemplosOpenApi.ERRO_403),
                            @ExampleObject(name = "Pedido em nome de outro cliente", value = ExemplosOpenApi.ERRO_403_PEDIDO_OUTRO_CLIENTE)})),
            @ApiResponse(responseCode = "404", description = "Unidade, produto ou cliente inexistente",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Produto inexistente", value = ExemplosOpenApi.ERRO_404_PRODUTO))),
            @ApiResponse(responseCode = "409", description = "Regra de negócio violada",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = {
                            @ExampleObject(name = "Estoque insuficiente", value = ExemplosOpenApi.ERRO_409_ESTOQUE_PEDIDO),
                            @ExampleObject(name = "Produto fora de época", value = ExemplosOpenApi.ERRO_409_FORA_DE_EPOCA),
                            @ExampleObject(name = "clienteId não é cliente", value = ExemplosOpenApi.ERRO_409_CLIENTE_INVALIDO)})),
            @ApiResponse(responseCode = "422", description = "Campos obrigatórios ausentes ou com formato inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Validação", value = ExemplosOpenApi.ERRO_422)))
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'ATENDENTE')")
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request,
                                                @Parameter(hidden = true) @AuthenticationPrincipal UsuarioDetailsImpl usuarioLogado) {
        var pedido = pedidoService.criarPedido(request, usuarioLogado.getUsuario());
        return ResponseEntity.status(HttpStatus.CREATED).body(PedidoResponse.from(pedido));
    }

    @Operation(summary = "Listar pedidos",
            description = "Qualquer perfil autenticado. Filtros opcionais por canalPedido e status. O perfil CLIENTE vê apenas os próprios pedidos.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de pedidos",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, examples = @ExampleObject(name = "Pedidos", value = ExemplosOpenApi.PAGINA_PEDIDOS))),
            @ApiResponse(responseCode = "400", description = "Filtro, paginação ou ordenação inválidos",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = {
                            @ExampleObject(name = "Status inválido", value = ExemplosOpenApi.ERRO_400_FILTRO),
                            @ExampleObject(name = "page inválido", value = ExemplosOpenApi.ERRO_400_PAGINACAO)})),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Sem token", value = ExemplosOpenApi.ERRO_401)))
    })
    @GetMapping
    public PaginaResponse<PedidoResponse> listar(@Parameter(description = "Filtra pelo canal de origem", example = "APP") @RequestParam(required = false) CanalPedido canalPedido,
                                                 @Parameter(description = "Filtra pelo status", example = "EM_PREPARO") @RequestParam(required = false) StatusPedido status,
                                                 @Parameter(description = "Página, começando em 1", example = "1") @RequestParam(defaultValue = "1") int page,
                                                 @Parameter(description = "Itens por página, de 1 a 100", example = "10") @RequestParam(defaultValue = "10") int limit,
                                                 @Parameter(description = "Ordenação: campo ou campo,asc|desc. Campos: id, criadoEm, total, status, canalPedido", example = "criadoEm,desc") @RequestParam(required = false) String sort,
                                                 @Parameter(hidden = true) @AuthenticationPrincipal UsuarioDetailsImpl usuarioLogado) {
        Pageable pageable = Paginacao.criar(page, limit, sort, CAMPOS_ORDENACAO, Sort.by(Sort.Direction.DESC, "id"));
        return PaginaResponse.de(
                pedidoService.listar(canalPedido, status, usuarioLogado.getUsuario(), pageable),
                PedidoResponse::from);
    }

    @Operation(summary = "Consultar pedido",
            description = "Qualquer perfil autenticado. O perfil CLIENTE só consulta os próprios pedidos: o pedido de outro cliente retorna 404, sem revelar que existe.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pedido encontrado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = PedidoResponse.class), examples = @ExampleObject(name = "Pedido", value = ExemplosOpenApi.PEDIDO_APROVADO))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Sem token", value = ExemplosOpenApi.ERRO_401))),
            @ApiResponse(responseCode = "404", description = "Pedido inexistente ou de outro cliente",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404)))
    })
    @GetMapping("/{id}")
    public PedidoResponse buscar(@Parameter(description = "Id do pedido", example = "1") @PathVariable Long id, @Parameter(hidden = true) @AuthenticationPrincipal UsuarioDetailsImpl usuarioLogado) {
        return PedidoResponse.from(pedidoService.buscarParaSolicitante(id, usuarioLogado.getUsuario()));
    }

    @Operation(summary = "Atualizar status do pedido",
            description = "Perfis ATENDENTE, GERENTE ou ADMIN. Transições permitidas: EM_PREPARO para PRONTO ou CANCELADO; PRONTO para ENTREGUE ou CANCELADO; AGUARDANDO_PAGAMENTO para CANCELADO. EM_PREPARO e PAGAMENTO_RECUSADO são definidos apenas pelo retorno do pagamento. Mudanças de status e cancelamentos são auditados.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status atualizado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = PedidoResponse.class), examples = @ExampleObject(name = "Pedido pronto", value = ExemplosOpenApi.PEDIDO_PRONTO))),
            @ApiResponse(responseCode = "400", description = "Corpo malformado ou valor de enum inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "JSON inválido", value = ExemplosOpenApi.ERRO_400_REQUISICAO))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Sem token", value = ExemplosOpenApi.ERRO_401))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão para a operação",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Perfil sem permissão", value = ExemplosOpenApi.ERRO_403))),
            @ApiResponse(responseCode = "404", description = "Pedido inexistente",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404))),
            @ApiResponse(responseCode = "409", description = "Transição de status não permitida",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = {
                            @ExampleObject(name = "Transição inválida", value = ExemplosOpenApi.ERRO_409_TRANSICAO_INVALIDA),
                            @ExampleObject(name = "Reservada ao sistema", value = ExemplosOpenApi.ERRO_409_TRANSICAO_RESERVADA)})),
            @ApiResponse(responseCode = "422", description = "Campos obrigatórios ausentes ou com formato inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Validação", value = ExemplosOpenApi.ERRO_422)))
    })
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GERENTE', 'ADMIN')")
    public PedidoResponse atualizarStatus(@Parameter(description = "Id do pedido", example = "1") @PathVariable Long id, @Valid @RequestBody AtualizarStatusRequest request) {
        return PedidoResponse.from(pedidoService.atualizarStatus(id, request.novoStatus()));
    }

    @Operation(summary = "Tentar o pagamento novamente",
            description = "Perfis CLIENTE ou ATENDENTE. Só para pedidos AGUARDANDO_PAGAMENTO, ou seja, após falha de comunicação com o gateway. Não cria um novo pedido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Pagamento processado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = PedidoResponse.class), examples = @ExampleObject(name = "Pagamento aprovado", value = ExemplosOpenApi.PEDIDO_APROVADO))),
            @ApiResponse(responseCode = "400", description = "Corpo malformado ou valor de enum inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "JSON inválido", value = ExemplosOpenApi.ERRO_400_REQUISICAO))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Sem token", value = ExemplosOpenApi.ERRO_401))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão para a operação",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Perfil sem permissão", value = ExemplosOpenApi.ERRO_403))),
            @ApiResponse(responseCode = "404", description = "Pedido inexistente ou de outro cliente",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Não encontrado", value = ExemplosOpenApi.ERRO_404))),
            @ApiResponse(responseCode = "409", description = "Pedido já teve o pagamento processado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Já processado", value = ExemplosOpenApi.ERRO_409_PEDIDO_JA_PROCESSADO))),
            @ApiResponse(responseCode = "422", description = "Campos obrigatórios ausentes ou com formato inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Validação", value = ExemplosOpenApi.ERRO_422)))
    })
    @PostMapping("/{id}/pagamento/retentar")
    @PreAuthorize("hasAnyRole('CLIENTE', 'ATENDENTE')")
    public PedidoResponse retentarPagamento(@Parameter(description = "Id do pedido", example = "1") @PathVariable Long id,
                                            @Valid @RequestBody RetentarPagamentoRequest request,
                                            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioDetailsImpl usuarioLogado) {
        return PedidoResponse.from(
                pedidoService.retentarPagamento(id, request.formaPagamento(), usuarioLogado.getUsuario()));
    }
}
