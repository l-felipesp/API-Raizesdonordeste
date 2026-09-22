package com.raizesdonordeste.backend.application.service;

import com.raizesdonordeste.backend.api.dto.EstoqueRequest;
import com.raizesdonordeste.backend.api.dto.MovimentacaoEstoqueRequest;
import com.raizesdonordeste.backend.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.backend.domain.exception.RegraDeNegocioException;
import com.raizesdonordeste.backend.domain.model.Estoque;
import com.raizesdonordeste.backend.domain.model.Produto;
import com.raizesdonordeste.backend.domain.model.Unidade;
import com.raizesdonordeste.backend.infrastructure.persistence.EstoqueRepository;
import com.raizesdonordeste.backend.infrastructure.persistence.ProdutoRepository;
import com.raizesdonordeste.backend.infrastructure.persistence.UnidadeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final UnidadeRepository unidadeRepository;
    private final ProdutoRepository produtoRepository;

    public EstoqueService(EstoqueRepository estoqueRepository, UnidadeRepository unidadeRepository,
                           ProdutoRepository produtoRepository) {
        this.estoqueRepository = estoqueRepository;
        this.unidadeRepository = unidadeRepository;
        this.produtoRepository = produtoRepository;
    }

    public List<Estoque> listarPorUnidade(Long unidadeId) {
        return estoqueRepository.findByUnidadeId(unidadeId);
    }

    @Transactional
    public Estoque criar(EstoqueRequest request) {
        Unidade unidade = unidadeRepository.findById(request.unidadeId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade não encontrada: id " + request.unidadeId()));
        Produto produto = produtoRepository.findById(request.produtoId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: id " + request.produtoId()));

        estoqueRepository.findByUnidadeIdAndProdutoId(unidade.getId(), produto.getId())
                .ifPresent(e -> {
                    throw new RegraDeNegocioException("ESTOQUE_JA_EXISTE",
                            "Já existe registro de estoque para este produto nesta unidade.");
                });

        Estoque estoque = Estoque.builder()
                .unidade(unidade)
                .produto(produto)
                .quantidade(request.quantidadeInicial())
                .build();
        return estoqueRepository.save(estoque);
    }

    @Transactional
    public Estoque movimentar(Long estoqueId, MovimentacaoEstoqueRequest request) {
        Estoque estoque = estoqueRepository.findById(estoqueId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Registro de estoque não encontrado: id " + estoqueId));

        switch (request.tipo()) {
            case ENTRADA -> estoque.adicionar(request.quantidade());
            case SAIDA -> estoque.debitar(request.quantidade());
        }
        return estoqueRepository.save(estoque);
    }
}