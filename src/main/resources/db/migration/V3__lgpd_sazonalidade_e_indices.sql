-- =====================================================================
-- V3 - LGPD (consentimento e anonimizacao), produtos sazonais e indice
-- para os relatorios da matriz.
-- =====================================================================

-- LGPD: registro do consentimento para uso de dados na fidelizacao.
-- consentimento_fidelizacao_em guarda o momento da ultima decisao do titular
-- (concessao ou revogacao), como evidencia do consentimento.
ALTER TABLE usuario ADD COLUMN consentimento_fidelizacao BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE usuario ADD COLUMN consentimento_fidelizacao_em TIMESTAMP(6);

-- LGPD: data em que o cadastro foi anonimizado a pedido do titular.
ALTER TABLE usuario ADD COLUMN anonimizado_em TIMESTAMP(6);

-- Produtos sazonais: fora do periodo o produto
-- nao aparece no cardapio e nao pode ser pedido. Nulo = sem restricao.
ALTER TABLE produto ADD COLUMN disponivel_de DATE;
ALTER TABLE produto ADD COLUMN disponivel_ate DATE;
ALTER TABLE produto ADD CONSTRAINT ck_produto_periodo_disponibilidade
    CHECK (disponivel_de IS NULL OR disponivel_ate IS NULL OR disponivel_ate >= disponivel_de);

-- Relatorios da matriz filtram pedidos por periodo.
CREATE INDEX idx_pedido_criado_em ON pedido (criado_em);
