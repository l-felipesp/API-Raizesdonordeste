package com.raizesdonordeste.backend.application.service;

import com.raizesdonordeste.backend.api.dto.ProdutoRequest;
import com.raizesdonordeste.backend.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.backend.domain.model.Produto;
import com.raizesdonordeste.backend.infrastructure.persistence.ProdutoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public Page<Produto> listar(Pageable pageable) {
        return produtoRepository.findAll(pageable);
    }

    public Produto buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado: id " + id));
    }

    public Produto criar(ProdutoRequest request) {
        Produto produto = Produto.builder()
                .nome(request.nome())
                .descricao(request.descricao())
                .preco(request.preco())
                .categoria(request.categoria())
                .build();
        return produtoRepository.save(produto);
    }

    public Produto atualizar(Long id, ProdutoRequest request) {
        Produto produto = buscarPorId(id);
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setPreco(request.preco());
        produto.setCategoria(request.categoria());
        return produtoRepository.save(produto);
    }
}