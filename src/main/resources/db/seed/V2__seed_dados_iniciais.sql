INSERT INTO usuario (nome, email, senha_hash, perfil, criado_em) VALUES
    ('Admin Demo',     'admin@raizes.com',     '$2a$10$MxqcDyZ524DlA8q39booCebASC8SLKJ6HvgLLnXxvZmmJBcvymos2', 'ADMIN',     NOW()),
    ('Gerente Demo',   'gerente@raizes.com',   '$2a$10$MxqcDyZ524DlA8q39booCebASC8SLKJ6HvgLLnXxvZmmJBcvymos2', 'GERENTE',   NOW()),
    ('Atendente Demo', 'atendente@raizes.com', '$2a$10$MxqcDyZ524DlA8q39booCebASC8SLKJ6HvgLLnXxvZmmJBcvymos2', 'ATENDENTE', NOW()),
    ('Cliente Demo',   'cliente@raizes.com',   '$2a$10$MxqcDyZ524DlA8q39booCebASC8SLKJ6HvgLLnXxvZmmJBcvymos2', 'CLIENTE',   NOW());

INSERT INTO unidade (nome, cidade, estado, tipo) VALUES
    ('Raízes Recife Centro',       'Recife',    'PE', 'COMPLETA'),
    ('Raízes Quiosque Shopping',   'Recife',    'PE', 'REDUZIDA'),
    ('Raízes Fortaleza Aldeota',   'Fortaleza', 'CE', 'COMPLETA');

INSERT INTO produto (nome, descricao, preco, categoria) VALUES
    ('Tapioca de Queijo Coalho',        'Tapioca recheada com queijo coalho',         12.50, 'Salgados'),
    ('Cuscuz com Carne de Sol',         'Cuscuz nordestino recheado com carne de sol', 18.90, 'Salgados'),
    ('Bolo de Macaxeira',               'Fatia de bolo de macaxeira',                  9.90, 'Doces'),
    ('Suco de Caju',                    'Suco natural de caju 400ml',                  8.50, 'Bebidas'),
    ('Café Coado',                      'Café passado na hora 200ml',                  5.00, 'Bebidas'),
    ('Baião de Dois',                   'Prato quente tradicional',                   32.00, 'Pratos Quentes');

INSERT INTO estoque (unidade_id, produto_id, quantidade)
SELECT u.id, p.id, 100
FROM unidade u CROSS JOIN produto p
WHERE u.nome = 'Raízes Recife Centro';

INSERT INTO estoque (unidade_id, produto_id, quantidade)
SELECT u.id, p.id, 80
FROM unidade u CROSS JOIN produto p
WHERE u.nome = 'Raízes Fortaleza Aldeota';

INSERT INTO estoque (unidade_id, produto_id, quantidade)
SELECT u.id, p.id, 50
FROM unidade u CROSS JOIN produto p
WHERE u.nome = 'Raízes Quiosque Shopping'
  AND p.categoria <> 'Pratos Quentes';
