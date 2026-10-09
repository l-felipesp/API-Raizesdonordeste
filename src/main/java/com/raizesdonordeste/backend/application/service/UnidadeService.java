package com.raizesdonordeste.backend.application.service;

import com.raizesdonordeste.backend.api.dto.ItemCardapioDTO;
import com.raizesdonordeste.backend.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.backend.domain.model.Unidade;
import com.raizesdonordeste.backend.infrastructure.persistence.EstoqueRepository;
import com.raizesdonordeste.backend.infrastructure.persistence.UnidadeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
public class UnidadeService {

    private final UnidadeRepository unidadeRepository;
    private final EstoqueRepository estoqueRepository;
    private final Clock clock;

    public UnidadeService(UnidadeRepository unidadeRepository, EstoqueRepository estoqueRepository, Clock clock) {
        this.unidadeRepository = unidadeRepository;
        this.estoqueRepository = estoqueRepository;
        this.clock = clock;
    }

    public Page<Unidade> listar(Pageable pageable) {
        return unidadeRepository.findAll(pageable);
    }

    public Unidade buscarPorId(Long id) {
        return unidadeRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Unidade não encontrada: id " + id));
    }

    public Unidade criar(Unidade unidade) {
        return unidadeRepository.save(unidade);
    }

    //Cardápio = produtos com estoque positivo nesta unidade e dentro do período de disponibilidade
    public List<ItemCardapioDTO> consultarCardapio(Long unidadeId) {
        buscarPorId(unidadeId); // valida que a unidade existe (senão já lança 404 aqui)
        LocalDate hoje = LocalDate.now(clock);
        return estoqueRepository.findByUnidadeId(unidadeId).stream()
                .filter(e -> e.getQuantidade() > 0)
                .filter(e -> e.getProduto().estaDisponivelEm(hoje))
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
