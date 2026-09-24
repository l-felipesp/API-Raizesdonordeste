package com.raizesdonordeste.backend.application.service;

import com.raizesdonordeste.backend.api.dto.PedidoItemRequest;
import com.raizesdonordeste.backend.api.dto.PedidoRequest;
import com.raizesdonordeste.backend.domain.enums.CanalPedido;
import com.raizesdonordeste.backend.domain.enums.StatusPedido;
import com.raizesdonordeste.backend.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.backend.domain.exception.RegraDeNegocioException;
import com.raizesdonordeste.backend.domain.model.*;
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

    public PedidoService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository,
                          UnidadeRepository unidadeRepository, ProdutoRepository produtoRepository,
                          EstoqueRepository estoqueRepository, PagamentoRepository pagamentoRepository,
                          PagamentoGatewayMockService gatewayPagamento) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.unidadeRepository = unidadeRepository;
        this.produtoRepository = produtoRepository;
        this.estoqueRepository = estoqueRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.gatewayPagamento = gatewayPagamento;
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

        List<Estoque> estoquesAfetados = new ArrayList<>();

        for (PedidoItemRequest itemReq : request.itens()) {
            Produto produto = produtoRepository.findById(itemReq.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Produto não encontrado: id " + itemReq.produtoId()));

            Estoque estoque = estoqueRepository.findByUnidadeIdAndProdutoId(unidade.getId(), produto.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Produto '" + produto.getNome() + "' não está disponível nesta unidade."));

            if (!estoque.temDisponibilidade(itemReq.quantidade())) {
                throw new com.raizesdonordeste.backend.domain.exception.EstoqueInsuficienteException(
                        "Estoque insuficiente para '" + produto.getNome() + "'. Disponível: " + estoque.getQuantidade());
            }

            ItemPedido item = ItemPedido.builder()
                    .produto(produto)
                    .quantidade(itemReq.quantidade())
                    .precoUnitario(produto.getPreco())
                    .build();
            pedido.adicionarItem(item);
            estoquesAfetados.add(estoque);
        }

        pedido.calcularTotal();

        for (int i = 0; i < pedido.getItens().size(); i++) {
            Estoque estoque = estoquesAfetados.get(i);
            estoque.debitar(pedido.getItens().get(i).getQuantidade());
            estoqueRepository.save(estoque);
        }

        pedido = pedidoRepository.save(pedido);

        ResultadoPagamentoMock resultado = gatewayPagamento.processar(pedido.getTotal(), request.formaPagamento());

        Pagamento pagamento = Pagamento.builder()
                .pedido(pedido)
                .formaPagamento(request.formaPagamento())
                .build();

        if (resultado.aprovado()) {
            pagamento.aprovar();
            pedido.atualizarStatus(StatusPedido.EM_PREPARO);
        } else {
            pagamento.recusar();
            pedido.atualizarStatus(StatusPedido.PAGAMENTO_RECUSADO);
            for (int i = 0; i < pedido.getItens().size(); i++) {
                Estoque estoque = estoquesAfetados.get(i);
                estoque.estornar(pedido.getItens().get(i).getQuantidade());
                estoqueRepository.save(estoque);
            }
        }

        pagamentoRepository.save(pagamento);
        return pedidoRepository.save(pedido);
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
        Pedido pedido = buscarPorId(id);
        pedido.atualizarStatus(novoStatus);
        return pedidoRepository.save(pedido);
    }
}