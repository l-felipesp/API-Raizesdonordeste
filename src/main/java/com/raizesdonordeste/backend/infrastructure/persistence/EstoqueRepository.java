package com.raizesdonordeste.backend.infrastructure.persistence;

import com.raizesdonordeste.backend.domain.model.Estoque;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {
    Optional<Estoque> findByUnidadeIdAndProdutoId(Long unidadeId, Long produtoId);

    List<Estoque> findByUnidadeId(Long unidadeId);

    Page<Estoque> findByUnidadeId(Long unidadeId, Pageable pageable);
}
