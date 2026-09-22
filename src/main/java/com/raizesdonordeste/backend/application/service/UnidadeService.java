package com.raizesdonordeste.backend.application.service;

import com.raizesdonordeste.backend.api.dto.ItemCardapioDTO;
import com.raizesdonordeste.backend.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.backend.domain.model.Unidade;
import com.raizesdonordeste.backend.infrastructure.persistence.EstoqueRepository;
import com.raizesdonordeste.backend.infrastructure.persistence.UnidadeRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UnidadeService {

    private final UnidadeRepository unidadeRepository;
    private final EstoqueRepository estoqueRepository;

    public UnidadeService(UnidadeRepository unidadeRepository, EstoqueRepository estoqueRepository) {
        this.unidadeRepository = unidadeRepository;
        this.estoqueRepository = estoqueRepository;
    }

    public List<Unidade> listarTodas() {
        return unidadeRepository.findAll();
    }

    public Unidade buscarPorId(Long id) {
        return unidadeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade não encontrada: id " + id));
    }

    public Unidade criar(Unidade unidade) {
        return unidadeRepository.save(unidade);
    }

    public List<ItemCardapioDTO> consultarCardapio(Long unidadeId) {
        buscarPorId(unidadeId); // valida que a unidade existe (senão já lança 404 aqui)
        return estoqueRepository.findByUnidadeId(unidadeId).stream()
                .filter(e -> e.getQuantidade() > 0)
                .map(e -> new ItemCardapioDTO(
                        e.getProduto().getId(),
                        e.getProduto().getNome(),
                        e.getProduto().getDescricao(),
                        e.getProduto().getPreco(),
                        e.getProduto().getCategoria(),
                        e.getQuantidade()))
                .toList();
    }
}