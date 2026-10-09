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
import com.raizesdonordeste.backend.infrastructure.payment.GatewayIndisponivelException;
import com.raizesdonordeste.backend.infrastructure.payment.PagamentoGatewayMockService;
import com.raizesdonordeste.backend.infrastructure.payment.ResultadoPagamentoMock;
import com.raizesdonordeste.backend.infrastructure.persistence.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final PedidoRepository pedidoRepository;
    private final UsuarioRepository usuarioRepository;
    private final UnidadeRepository unidadeRepository;
    private final ProdutoRepository produtoRepository;
    private final EstoqueRepository estoqueRepository;
    private final PagamentoRepository pagamentoRepository;
    private final PagamentoGatewayMockService gatewayPagamento;
    private final AuditLogService auditLogService;
    private final Clock clock;

    public PedidoService(PedidoRepository pedidoRepository, UsuarioRepository usuarioRepository,
                         UnidadeRepository unidadeRepository, ProdutoRepository produtoRepository,
                         EstoqueRepository estoqueRepository, PagamentoRepository pagamentoRepository,
                         PagamentoGatewayMockService gatewayPagamento, AuditLogService auditLogService,
                         Clock clock) {
        this.pedidoRepository = pedidoRepository;
        this.usuarioRepository = usuarioRepository;
        this.unidadeRepository = unidadeRepository;
        this.produtoRepository = produtoRepository;
        this.estoqueRepository = estoqueRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.gatewayPagamento = gatewayPagamento;
        this.auditLogService = auditLogService;
        this.clock = clock;
    }

    @Transactional
    public Pedido criarPedido(PedidoRequest request, Usuario solicitante) {
        Usuario cliente = resolverCliente(request.clienteId(), solicitante);

        Unidade unidade = unidadeRepository.findById(request.unidadeId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade não encontrada: id " + request.unidadeId()));

        Pedido pedido = Pedido.builder()
                .cliente(cliente)
                .unidade(unidade)
                .canalPedido(request.canalPedido())
                .build();

        LocalDate hoje = LocalDate.now(clock);
        for (int i = 0; i < request.itens().size(); i++) {
            PedidoItemRequest itemReq = request.itens().get(i);
            Produto produto = produtoRepository.findById(itemReq.produtoId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Produto não encontrado: id " + itemReq.produtoId()));

            if (!produto.estaDisponivelEm(hoje)) {
                throw new RegraDeNegocioException("PRODUTO_FORA_DE_EPOCA",
                        "O produto '" + produto.getNome() + "' é sazonal e não está disponível hoje.",
                        "itens[" + i + "].produtoId", "Disponível " + descreverPeriodo(produto));
            }

            estoqueRepository.findByUnidadeIdAndProdutoId(unidade.getId(), produto.getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Produto '" + produto.getNome() + "' não está disponível nesta unidade."));

            ItemPedido item = ItemPedido.builder()
                    .produto(produto)
                    .quantidade(itemReq.quantidade())
                    .precoUnitario(produto.getPreco()) // preço sempre do cadastro, nunca do request
                    .build();
            pedido.adicionarItem(item);
        }

        pedido.calcularTotal();
        pedido = pedidoRepository.save(pedido);

        auditLogService.registrar("PEDIDO_CRIADO", "Pedido", pedido.getId(),
                "canal=" + pedido.getCanalPedido() + "; total=" + pedido.getTotal()
                        + "; clienteId=" + cliente.getId());

        return processarPagamento(pedido, request.formaPagamento());
    }

    //Define o cliente do pedido:
    //CLIENTE: sempre ele mesmo (não pode criar pedido em nome de outro cliente)
    //ATENDENTE com clienteId: pedido de balcão em nome de um cliente cadastrado
    //ATENDENTE sem clienteId: cliente de balcão sem cadastro, registrado no nome do atendente
    private Usuario resolverCliente(Long clienteIdInformado, Usuario solicitante) {
        if (solicitante.getPerfil() == Perfil.CLIENTE) {
            if (clienteIdInformado != null && !clienteIdInformado.equals(solicitante.getId())) {
                throw new AcessoNegadoException("Clientes só podem criar pedidos para si mesmos.");
            }
            return buscarUsuario(solicitante.getId());
        }
        if (clienteIdInformado == null) {
            return buscarUsuario(solicitante.getId());
        }
        Usuario cliente = usuarioRepository.findById(clienteIdInformado)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente não encontrado: id " + clienteIdInformado));
        if (cliente.getPerfil() != Perfil.CLIENTE || cliente.isAnonimizado()) {
            throw new RegraDeNegocioException("CLIENTE_INVALIDO",
                    "O usuário informado não é um cliente ativo.", "clienteId", "Não é um cliente ativo");
        }
        return cliente;
    }

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário autenticado não encontrado."));
    }

    private String descreverPeriodo(Produto produto) {
        if (produto.getDisponivelDe() != null && produto.getDisponivelAte() != null) {
            return "de " + produto.getDisponivelDe().format(DATA_BR) + " a " + produto.getDisponivelAte().format(DATA_BR);
        }
        if (produto.getDisponivelDe() != null) {
            return "a partir de " + produto.getDisponivelDe().format(DATA_BR);
        }
        return "até " + produto.getDisponivelAte().format(DATA_BR);
    }

    @Transactional
    public Pedido retentarPagamento(Long pedidoId, String novaFormaPagamento, Usuario solicitante) {
        Pedido pedido = buscarParaSolicitante(pedidoId, solicitante);

        if (pedido.getStatus() != StatusPedido.AGUARDANDO_PAGAMENTO) {
            throw new RegraDeNegocioException("PEDIDO_JA_PROCESSADO",
                    "Este pedido já teve o pagamento processado (status atual: " + pedido.getStatus() + "). Não é possível retentar.");
        }

        auditLogService.registrar("PAGAMENTO_RETENTADO", "Pedido", pedido.getId(),
                "novaFormaPagamento=" + novaFormaPagamento);

        return processarPagamento(pedido, novaFormaPagamento);
    }

    //Debita o estoque, solicita o pagamento ao gateway mock e trata os três desfechos. Usado na criação e na nova tentativa.
    private Pedido processarPagamento(Pedido pedido, String formaPagamento) {
        List<Estoque> estoquesAfetados = new ArrayList<>();
        for (int i = 0; i < pedido.getItens().size(); i++) {
            ItemPedido item = pedido.getItens().get(i);
            Estoque estoque = estoqueRepository
                    .findByUnidadeIdAndProdutoId(pedido.getUnidade().getId(), item.getProduto().getId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException(
                            "Produto '" + item.getProduto().getNome() + "' não está mais disponível nesta unidade."));
            if (!estoque.temDisponibilidade(item.getQuantidade())) {
                throw new EstoqueInsuficienteException(
                        "Estoque insuficiente para '" + item.getProduto().getNome() + "'.",
                        "itens[" + i + "].quantidade", "Disponível: " + estoque.getQuantidade());
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
            // Falha de comunicação: pedido continua AGUARDANDO_PAGAMENTO, estoque é devolvido e
            // o pagamento fica PENDENTE, permitindo nova tentativa sem duplicar o pedido.
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

    // Filtros opcionais por canal e status. Um CLIENTE só enxerga os próprios pedidos.
    public Page<Pedido> listar(CanalPedido canalPedido, StatusPedido status, Usuario solicitante, Pageable pageable) {
        Specification<Pedido> filtro = PedidoSpecifications.doCanal(canalPedido)
                .and(PedidoSpecifications.comStatus(status));
        if (solicitante.getPerfil() == Perfil.CLIENTE) {
            filtro = filtro.and(PedidoSpecifications.doCliente(solicitante.getId()));
        }
        return pedidoRepository.findAll(filtro, pageable);
    }

    //Busca o pedido respeitando o dono: um CLIENTE recebe 404 para pedidos de outros clientes, sem revelar que o pedido existe.
    public Pedido buscarParaSolicitante(Long id, Usuario solicitante) {
        Pedido pedido = buscarPorId(id);
        if (solicitante.getPerfil() == Perfil.CLIENTE
                && !pedido.getCliente().getId().equals(solicitante.getId())) {
            throw new RecursoNaoEncontradoException("Pedido não encontrado: id " + id);
        }
        return pedido;
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
        StatusPedido statusAnterior = pedido.getStatus();
        pedido.atualizarStatus(novoStatus);
        pedidoRepository.save(pedido);
        auditLogService.registrar(novoStatus == StatusPedido.CANCELADO ? "PEDIDO_CANCELADO" : "STATUS_ATUALIZADO",
                "Pedido", pedido.getId(), "de=" + statusAnterior + "; para=" + novoStatus);
        return pedido;
    }
}
