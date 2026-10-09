package com.raizesdonordeste.backend.infrastructure.persistence;

import com.raizesdonordeste.backend.domain.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByEntidadeAndEntidadeId(String entidade, Long entidadeId, Pageable pageable);

    // LGPD: ao anonimizar um cadastro, o e-mail no histórico de auditoria vira o mesmo pseudônimo,
    // preservando a rastreabilidade das ações sem manter o dado pessoal.
    @Modifying
    @Query("UPDATE AuditLog a SET a.usuarioEmail = :pseudonimo WHERE a.usuarioEmail = :emailOriginal")
    int pseudonimizarResponsavel(@Param("emailOriginal") String emailOriginal, @Param("pseudonimo") String pseudonimo);
}
