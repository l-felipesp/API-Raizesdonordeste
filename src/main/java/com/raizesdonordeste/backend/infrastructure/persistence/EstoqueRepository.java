package com.raizesdonordeste.backend.infrastructure.persistence;

import com.raizesdonordeste.backend.domain.model.Estoque;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {
    Optional<Estoque> findByUnidadeIdAndProdutoId(Long unidadeId, Long produtoId);    
    List<Estoque> findByUnidadeId(Long unidadeId);}