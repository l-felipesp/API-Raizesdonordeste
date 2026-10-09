package com.raizesdonordeste.backend.domain.model;

import com.raizesdonordeste.backend.domain.enums.Perfil;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Perfil perfil;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    // LGPD: consentimento para uso de dados na fidelização e momento da última decisão do titular
    @Column(name = "consentimento_fidelizacao", nullable = false)
    private boolean consentimentoFidelizacao;

    @Column(name = "consentimento_fidelizacao_em")
    private LocalDateTime consentimentoFidelizacaoEm;

    // LGPD: preenchido quando o titular pede a exclusão dos seus dados pessoais
    @Column(name = "anonimizado_em")
    private LocalDateTime anonimizadoEm;

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }

    public void registrarDecisaoFidelizacao(boolean aceita, LocalDateTime momento) {
        this.consentimentoFidelizacao = aceita;
        this.consentimentoFidelizacaoEm = momento;
    }

    public boolean isAnonimizado() {
        return anonimizadoEm != null;
    }

    //Remove os dados pessoais do cadastro, mantendo o registro (e os pedidos ligados a ele)
    //E-mail vira um pseudônimo único e a senha é substituída por um hash inutilizável, impedindo novos logins.
    public void anonimizar(String senhaHashInutilizavel, LocalDateTime momento) {
        if (isAnonimizado()) {
            throw new com.raizesdonordeste.backend.domain.exception.RegraDeNegocioException(
                    "USUARIO_JA_ANONIMIZADO", "Este cadastro já foi anonimizado.");
        }
        this.nome = "Usuário removido";
        this.email = emailAnonimizado();
        this.senhaHash = senhaHashInutilizavel;
        this.consentimentoFidelizacao = false;
        this.consentimentoFidelizacaoEm = momento;
        this.anonimizadoEm = momento;
    }

    public String emailAnonimizado() {
        return "anonimizado-" + this.id + "@removido.invalid";
    }
}