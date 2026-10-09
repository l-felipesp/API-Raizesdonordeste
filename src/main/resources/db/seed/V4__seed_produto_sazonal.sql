-- =====================================================================
-- V4 - Seed: produto sazonal de exemplo (periodo junino de 2026).
-- Como o periodo ja terminou, o produto fica fora do cardapio e o pedido
-- dele retorna 409 PRODUTO_FORA_DE_EPOCA, de forma reproduzivel em
-- qualquer data posterior a julho de 2026.
-- =====================================================================

INSERT INTO produto (nome, descricao, preco, categoria, disponivel_de, disponivel_ate) VALUES
    ('Canjica Junina', 'Canjica de milho com canela - edição do período junino', 11.90, 'Doces',
     DATE '2026-06-01', DATE '2026-07-31');

INSERT INTO estoque (unidade_id, produto_id, quantidade)
SELECT u.id, p.id, 40
FROM unidade u CROSS JOIN produto p
WHERE u.nome = 'Raízes Recife Centro'
  AND p.nome = 'Canjica Junina';
