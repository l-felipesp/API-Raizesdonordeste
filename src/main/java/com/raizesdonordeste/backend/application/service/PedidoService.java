package com.raizesdonordeste.backend.application.service;

import com.raizesdonordeste.backend.api.dto.PedidoItemRequest;
import com.raizesdonordeste.backend.api.dto.PedidoRequest;
import com.raizesdonordeste.backend.domain.enums.CanalPedido;
import com.raizesdonordeste.backend.domain.enums.StatusPagamento;
import com.raizesdonordeste.backend.domain.enums.StatusPedido;
import com.raizesdonordeste.backend.domain.exception.EstoqueInsuficienteException;
import com.raizesdonordeste.backend.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.backend.domain.exception.RegraDeNegocioException;
import com.raizesdonordeste.backend.domain.model.*;
import com.raizesdonordeste.backend.infrastructure.payment.GatewayIndisponivelException;
import com.raizesdonordeste.backend.infrastructure.payment.PagamentoGatewayMockService;
import com.raizesdonordeste.backend.infrastructure.payment.ResultadoPagamentoMock;
import com.raizesdonordeste.backend.infrastructure.persistence.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UnidadeRepository unidadeRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;
    private final PagamentoRepository pagamentoRepository;
    private final PagamentoGatewayMockService gatewayPagamento;
    private final AuditLogService auditLogService;

    public PedidoService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository,
                          UnidadeRepository unidadeRepository, ProdutoRepository produtoRepository,
                          EstoqueRepository estoqueRepository, PagamentoRepository pagamentoRepository,
                          PagamentoGatewayMockService gatewayPagamento, AuditLogService auditLogService) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.unidadeRepository = unidadeRepository;
        this.produtoRepository = produtoRepository;
        this.estoqueRepository = estoqueRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.gatewayPagamento = gatewayPagamento;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public Pedido criarPedido(PedidoRequest request, String emailCliente) {
        Usuario cliente = usuarioRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado."));

        Unidade unidade = unidadeRepository.findById(request.unidadeId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade não encontrada: id " + request.unidadeId()));

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .unidade(unidade)
                .canalPedido(request.canalPedido())
                .build();

        for (PedidoItemRequest itemReq : request.itens()) {
            Produto produto = produtoRepository.findById(itemReq.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Produto não encontrado: id " + itemReq.produtoId()));

            estoqueRepository.findByUnidadeIdAndProdutoId(unidade.getId(), produto.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Produto '" + produto.getNome() + "' não está disponível nesta unidade."));

            ItemPedido item = ItemPedido.builder()
                    .produto(produto)
                    .quantidade(itemReq.quantidade())
                    .precoUnitario(produto.getPreco())
                    .build();
            pedido.adicionarItem(item);
        }

        pedido.calcularTotal();
        pedido = pedidoRepository.save(pedido);

        auditLogService.registrar("PEDIDO_CRIADO", "Pedido", pedido.getId(),
                "canal=" + pedido.getCanalPedido() + "; total=" + pedido.getTotal());

        return processarPagamento(pedido, request.formaPagamento());
    }

    @Transactional
    public Pedido retentarPagamento(Long pedidoId, String novaFormaPagamento) {
        Pedido pedido = buscarPorId(pedidoId);

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new RegraDeNegocioException("PEDIDO_JA_PROCESSADO",
                "Este pedido já teve o pagamento processado (status atual: " + pedido.getStatus() + "). Não é possível retentar.");
        }

        auditLogService.registrar("PAGAMENTO_RETENTADO", "Pedido", pedido.getId(),
            "novaFormaPagamento=" + novaFormaPagamento);

        return processarPagamento(pedido, novaFormaPagamento);
    }

    private Pedido processarPagamento(Pedido pedido, String formaPagamento) {
        List<Estoque> estoquesAfetados = new ArrayList<>();
        for (ItemPedido item : pedido.getItens()) {
            Estoque estoque = estoqueRepository
                .findByUnidadeIdAndProdutoId(pedido.getUnidade().getId(), item.getProduto().getId())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Produto '" + item.getProduto().getNome() + "' não está mais disponível nesta unidade."));
        if (!estoque.temDisponibilidade(item.getQuantidade())) {
            throw new EstoqueInsuficienteException(
                    "Estoque insuficiente para '" + item.getProduto().getNome() + "'. Disponível: " + estoque.getQuantidade());
        }
        estoquesAfetados.add(estoque);
        }

        for (int i = 0; i < pedido.getItens().size(); i++) {
            estoquesAfetados.get(i).debitar(pedido.getItens().get(i).getQuantidade());
            estoqueRepository.save(estoquesAfetados.get(i));
        }

        Pagamento pagamento = pagamentoRepository.findByPedidoId(pedido.getId())
            .orElseGet(() -> Pagamento.builder().pedido(pedido).formaPagamento(formaPagamento).build());
        pagamento.setFormaPagamento(formaPagamento);

        try {
            ResultadoPagamentoMock resultado = gatewayPagamento.processar(pedido.getTotal(), formaPagamento);

            if (resultado.aprovado()) {
                pagamento.aprovar();
                pedido.atualizarStatus(StatusPedido.EM_PREPARO);
            } else {
                pagamento.recusar();
                pedido.atualizarStatus(StatusPedido.PAGAMENTO_RECUSADO);
                estornarEstoque(estoquesAfetados, pedido);
            }
        } catch (GatewayIndisponivelException ex) {
            pagamento.setStatus(StatusPagamento.PENDENTE);
            estornarEstoque(estoquesAfetados, pedido);
        }

        pagamentoRepository.save(pagamento);
        pedido.setPagamento(pagamento);

        auditLogService.registrar("PAGAMENTO_PROCESSADO", "Pedido", pedido.getId(),
            "formaPagamento=" + formaPagamento + "; resultado=" + pagamento.getStatus());

        return pedidoRepository.save(pedido);
    }

    private void estornarEstoque(List<Estoque> estoques, Pedido pedido) {
        for (int i = 0; i < pedido.getItens().size(); i++) {
            estoques.get(i).estornar(pedido.getItens().get(i).getQuantidade());
            estoqueRepository.save(estoques.get(i));
        }
    }

    public Page<Pedido> listar(CanalPedido canalPedido, Pageable pageable) {
        if (canalPedido != null) {
            return pedidoRepository.findByCanalPedido(canalPedido, pageable);
        }
        return pedidoRepository.findAll(pageable);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido não encontrado: id " + id));
    }

    @Transactional
    public Pedido atualizarStatus(Long id, StatusPedido novoStatus) {
        if (novoStatus == StatusPedido.EM_PREPARO || novoStatus == StatusPedido.PAGAMENTO_RECUSADO) {
            throw new RegraDeNegocioException("TRANSICAO_RESERVADA_AO_SISTEMA",
                "O status " + novoStatus + " só pode ser definido automaticamente pelo retorno do gateway de pagamento.");
        }
        Pedido pedido = buscarPorId(id);
        pedido.atualizarStatus(novoStatus);
        pedidoRepository.save(pedido);
        auditLogService.registrar("STATUS_ATUALIZADO", "Pedido", pedido.getId(), "novoStatus=" + novoStatus);
        return pedido;
    }
}