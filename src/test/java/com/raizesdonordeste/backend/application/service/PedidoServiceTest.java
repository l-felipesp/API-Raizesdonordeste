package com.raizesdonordeste.backend.application.service;

import com.raizesdonordeste.backend.api.dto.PedidoItemRequest;
import com.raizesdonordeste.backend.api.dto.PedidoRequest;
import com.raizesdonordeste.backend.domain.enums.CanalPedido;
import com.raizesdonordeste.backend.domain.enums.Perfil;
import com.raizesdonordeste.backend.domain.enums.StatusPagamento;
import com.raizesdonordeste.backend.domain.enums.StatusPedido;
import com.raizesdonordeste.backend.domain.exception.AcessoNegadoException;
import com.raizesdonordeste.backend.domain.exception.EstoqueInsuficienteException;
import com.raizesdonordeste.backend.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.backend.domain.exception.RegraDeNegocioException;
import com.raizesdonordeste.backend.domain.model.*;
import com.raizesdonordeste.backend.infrastructure.payment.PagamentoGatewayMockService;
import com.raizesdonordeste.backend.infrastructure.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Fluxo crítico: pedido -> pagamento mock -> status. Os repositórios são simulados; o gateway
 * mock e a auditoria são os reais. A data é fixada em 09/10/2026.
 */
class PedidoServiceTest {

    private PedidoRepository pedidoRepository;
    private UsuarioRepository usuarioRepository;
    private UnidadeRepository unidadeRepository;
    private ProdutoRepository produtoRepository;
    private EstoqueRepository estoqueRepository;
    private PagamentoRepository pagamentoRepository;
    private PedidoService service;

    private Usuario cliente;
    private Usuario outroCliente;
    private Usuario atendente;
    private Estoque estoqueTapioca;

    @BeforeEach
    void prepara() {
        pedidoRepository = mock(PedidoRepository.class);
        usuarioRepository = mock(UsuarioRepository.class);
        unidadeRepository = mock(UnidadeRepository.class);
        produtoRepository = mock(ProdutoRepository.class);
        estoqueRepository = mock(EstoqueRepository.class);
        pagamentoRepository = mock(PagamentoRepository.class);
        AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);

        Clock clock = Clock.fixed(Instant.parse("2026-10-09T15:00:00Z"), ZoneId.of("America/Sao_Paulo"));
        service = new PedidoService(pedidoRepository, usuarioRepository, unidadeRepository, produtoRepository,
                estoqueRepository, pagamentoRepository, new PagamentoGatewayMockService(),
                new AuditLogService(auditLogRepository), clock);

        cliente = Usuario.builder().id(4L).nome("Cliente Demo").perfil(Perfil.CLIENTE).build();
        outroCliente = Usuario.builder().id(5L).nome("Outro Cliente").perfil(Perfil.CLIENTE).build();
        atendente = Usuario.builder().id(3L).nome("Atendente Demo").perfil(Perfil.ATENDENTE).build();
        Unidade recife = Unidade.builder().id(1L).nome("Raízes Recife Centro").tipo("COMPLETA").build();
        Produto tapioca = Produto.builder().id(1L).nome("Tapioca de Queijo Coalho").preco(new BigDecimal("12.50")).build();
        Produto canjica = Produto.builder().id(7L).nome("Canjica Junina").preco(new BigDecimal("11.90"))
                .disponivelDe(LocalDate.of(2026, 6, 1)).disponivelAte(LocalDate.of(2026, 7, 31)).build();
        estoqueTapioca = Estoque.builder().id(1L).unidade(recife).produto(tapioca).quantidade(100).build();

        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(outroCliente));
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(atendente));
        when(unidadeRepository.findById(1L)).thenReturn(Optional.of(recife));
        when(produtoRepository.findById(1L)).thenReturn(Optional.of(tapioca));
        when(produtoRepository.findById(7L)).thenReturn(Optional.of(canjica));
        when(estoqueRepository.findByUnidadeIdAndProdutoId(1L, 1L)).thenReturn(Optional.of(estoqueTapioca));
        when(pagamentoRepository.findByPedidoId(anyLong())).thenReturn(Optional.empty());
        when(pagamentoRepository.save(any(Pagamento.class))).thenAnswer(inv -> inv.getArgument(0));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(inv -> {
            Pedido pedido = inv.getArgument(0);
            if (pedido.getId() == null) {
                pedido.setId(10L);
            }
            return pedido;
        });
    }

    private PedidoRequest pedido(Long produtoId, int quantidade, String formaPagamento, Long clienteId) {
        return new PedidoRequest(1L, CanalPedido.APP, clienteId,
                List.of(new PedidoItemRequest(produtoId, quantidade)), formaPagamento);
    }

    @Test
    @DisplayName("Pagamento aprovado: pedido EM_PREPARO e estoque debitado")
    void pagamentoAprovado() {
        Pedido pedido = service.criarPedido(pedido(1L, 2, "PIX", null), cliente);

        assertEquals(StatusPedido.EM_PREPARO, pedido.getStatus());
        assertEquals(StatusPagamento.APROVADO, pedido.getPagamento().getStatus());
        assertEquals(0, new BigDecimal("25.00").compareTo(pedido.getTotal()));
        assertEquals(98, estoqueTapioca.getQuantidade());
        assertEquals(cliente, pedido.getCliente());
    }

    @Test
    @DisplayName("Pagamento recusado (total a partir de R$ 500): status PAGAMENTO_RECUSADO e estoque devolvido")
    void pagamentoRecusado() {
        Pedido pedido = service.criarPedido(pedido(1L, 40, "CARTAO", null), cliente);

        assertEquals(StatusPedido.PAGAMENTO_RECUSADO, pedido.getStatus());
        assertEquals(StatusPagamento.RECUSADO, pedido.getPagamento().getStatus());
        assertEquals(100, estoqueTapioca.getQuantidade());
    }

    @Test
    @DisplayName("Falha de comunicação com o gateway: pedido aguarda pagamento e estoque é devolvido")
    void gatewayIndisponivel() {
        Pedido pedido = service.criarPedido(pedido(1L, 1, "PAGAMENTO_INDISPONIVEL", null), cliente);

        assertEquals(StatusPedido.AGUARDANDO_PAGAMENTO, pedido.getStatus());
        assertEquals(StatusPagamento.PENDENTE, pedido.getPagamento().getStatus());
        assertEquals(100, estoqueTapioca.getQuantidade());
    }

    @Test
    @DisplayName("Nova tentativa após falha do gateway aprova o mesmo pedido")
    void novaTentativaAposFalha() {
        Pedido pendente = service.criarPedido(pedido(1L, 2, "PAGAMENTO_INDISPONIVEL", null), cliente);
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pendente));
        when(pagamentoRepository.findByPedidoId(10L)).thenReturn(Optional.of(pendente.getPagamento()));

        Pedido pedido = service.retentarPagamento(10L, "PIX", cliente);

        assertEquals(10L, pedido.getId());
        assertEquals(StatusPedido.EM_PREPARO, pedido.getStatus());
        assertEquals(StatusPagamento.APROVADO, pedido.getPagamento().getStatus());
        assertEquals("PIX", pedido.getPagamento().getFormaPagamento());
        assertEquals(98, estoqueTapioca.getQuantidade());
    }

    @Test
    @DisplayName("Estoque insuficiente: pedido recusado com o item indicado e estoque intacto")
    void estoqueInsuficiente() {
        EstoqueInsuficienteException erro = assertThrows(EstoqueInsuficienteException.class,
                () -> service.criarPedido(pedido(1L, 101, "PIX", null), cliente));

        assertEquals("itens[0].quantidade", erro.getCampo());
        assertEquals("Disponível: 100", erro.getProblema());
        assertEquals(100, estoqueTapioca.getQuantidade());
    }

    @Test
    @DisplayName("Produto sazonal fora de época não pode ser pedido")
    void produtoForaDeEpoca() {
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> service.criarPedido(pedido(7L, 1, "PIX", null), cliente));

        assertEquals("PRODUTO_FORA_DE_EPOCA", erro.getCodigoErro());
        assertEquals("itens[0].produtoId", erro.getCampo());
        assertEquals("Disponível de 01/06/2026 a 31/07/2026", erro.getProblema());
    }

    @Test
    @DisplayName("Produto inexistente retorna recurso não encontrado")
    void produtoInexistente() {
        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.criarPedido(pedido(999L, 1, "PIX", null), cliente));
    }

    @Test
    @DisplayName("Atendente registra pedido de balcão em nome de um cliente")
    void atendenteEmNomeDoCliente() {
        Pedido pedido = service.criarPedido(pedido(1L, 1, "DINHEIRO", 4L), atendente);

        assertEquals(cliente, pedido.getCliente());
    }

    @Test
    @DisplayName("Cliente não pode criar pedido em nome de outro cliente")
    void clienteEmNomeDeOutro() {
        assertThrows(AcessoNegadoException.class,
                () -> service.criarPedido(pedido(1L, 1, "PIX", 5L), cliente));
        assertEquals(100, estoqueTapioca.getQuantidade());
    }

    @Test
    @DisplayName("Cliente não enxerga pedido de outro cliente")
    void pedidoDeOutroCliente() {
        Pedido pedidoDoCliente = Pedido.builder().id(10L).cliente(cliente).status(StatusPedido.EM_PREPARO).build();
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pedidoDoCliente));

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarParaSolicitante(10L, outroCliente));
        assertEquals(pedidoDoCliente, service.buscarParaSolicitante(10L, cliente));
        assertEquals(pedidoDoCliente, service.buscarParaSolicitante(10L, atendente));
    }

    @Test
    @DisplayName("EM_PREPARO só pode ser definido pelo retorno do pagamento")
    void statusReservadoAoSistema() {
        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> service.atualizarStatus(10L, StatusPedido.EM_PREPARO));

        assertEquals("TRANSICAO_RESERVADA_AO_SISTEMA", erro.getCodigoErro());
    }

    @Test
    @DisplayName("Não permite nova tentativa de pagamento em pedido já processado")
    void retentarPedidoJaProcessado() {
        Pedido pago = Pedido.builder().id(10L).cliente(cliente).status(StatusPedido.EM_PREPARO).build();
        when(pedidoRepository.findById(10L)).thenReturn(Optional.of(pago));

        RegraDeNegocioException erro = assertThrows(RegraDeNegocioException.class,
                () -> service.retentarPagamento(10L, "PIX", cliente));

        assertEquals("PEDIDO_JA_PROCESSADO", erro.getCodigoErro());
    }
}
