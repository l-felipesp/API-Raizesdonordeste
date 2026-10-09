package com.raizesdonordeste.backend.api.doc;

/** Exemplos de JSON exibidos no Swagger. Refletem o formato real das respostas da API. */
public final class ExemplosOpenApi {

    private ExemplosOpenApi() {
    }

    public static final String JSON = "application/json";

    // ---------------------------------------------------------------- requests

    public static final String REQ_PEDIDO_APP = """
            {"unidadeId": 1, "canalPedido": "APP", "formaPagamento": "PIX",
             "itens": [{"produtoId": 1, "quantidade": 2}]}""";

    public static final String REQ_PEDIDO_RECUSADO = """
            {"unidadeId": 1, "canalPedido": "TOTEM", "formaPagamento": "CARTAO",
             "itens": [{"produtoId": 1, "quantidade": 45}]}""";

    public static final String REQ_PEDIDO_GATEWAY_FORA = """
            {"unidadeId": 1, "canalPedido": "WEB", "formaPagamento": "PAGAMENTO_INDISPONIVEL",
             "itens": [{"produtoId": 1, "quantidade": 1}]}""";

    public static final String REQ_PEDIDO_BALCAO = """
            {"unidadeId": 1, "canalPedido": "BALCAO", "clienteId": 4, "formaPagamento": "DINHEIRO",
             "itens": [{"produtoId": 2, "quantidade": 1}, {"produtoId": 4, "quantidade": 1}]}""";

    // ---------------------------------------------------------------- respostas de sucesso

    public static final String LOGIN_OK = """
            {"accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjbGllbnRlQHJhaXplcy5jb20ifQ.assinatura",
             "tokenType": "Bearer", "expiresIn": 3600,
             "user": {"id": 4, "nome": "Cliente Demo", "perfil": "CLIENTE"}}""";

    public static final String USUARIO_CRIADO = """
            {"id": 5, "nome": "Maria Silva", "perfil": "CLIENTE"}""";

    public static final String USUARIO_ME = """
            {"id": 5, "nome": "Maria Silva", "email": "maria@exemplo.com", "perfil": "CLIENTE",
             "criadoEm": "2026-10-09T10:00:00", "consentimentoFidelizacao": true,
             "consentimentoFidelizacaoEm": "2026-10-09T10:00:00"}""";

    public static final String UNIDADE = """
            {"id": 1, "nome": "Raízes Recife Centro", "cidade": "Recife", "estado": "PE", "tipo": "COMPLETA"}""";

    public static final String PAGINA_UNIDADES = """
            {"itens": [
               {"id": 1, "nome": "Raízes Recife Centro", "cidade": "Recife", "estado": "PE", "tipo": "COMPLETA"},
               {"id": 2, "nome": "Raízes Quiosque Shopping", "cidade": "Recife", "estado": "PE", "tipo": "REDUZIDA"}],
             "page": 1, "limit": 2, "totalItens": 3, "totalPaginas": 2}""";

    public static final String CARDAPIO = """
            [{"produtoId": 1, "nome": "Tapioca de Queijo Coalho", "descricao": "Tapioca recheada com queijo coalho",
              "preco": 12.50, "categoria": "Salgados", "quantidadeDisponivel": 100},
             {"produtoId": 4, "nome": "Suco de Caju", "descricao": "Suco natural de caju 400ml",
              "preco": 8.50, "categoria": "Bebidas", "quantidadeDisponivel": 100}]""";

    public static final String PRODUTO = """
            {"id": 1, "nome": "Tapioca de Queijo Coalho", "descricao": "Tapioca recheada com queijo coalho",
             "preco": 12.50, "categoria": "Salgados", "disponivelDe": null, "disponivelAte": null}""";

    public static final String PRODUTO_SAZONAL = """
            {"id": 7, "nome": "Canjica Junina", "descricao": "Canjica de milho com canela",
             "preco": 11.90, "categoria": "Doces", "disponivelDe": "2026-06-01", "disponivelAte": "2026-07-31"}""";

    public static final String PAGINA_PRODUTOS = """
            {"itens": [
               {"id": 6, "nome": "Baião de Dois", "descricao": "Prato quente tradicional", "preco": 32.00,
                "categoria": "Pratos Quentes", "disponivelDe": null, "disponivelAte": null}],
             "page": 1, "limit": 1, "totalItens": 7, "totalPaginas": 7}""";

    public static final String ESTOQUE = """
            {"id": 1, "unidadeId": 1, "produtoId": 1, "nomeProduto": "Tapioca de Queijo Coalho", "quantidade": 150}""";

    public static final String PAGINA_ESTOQUE = """
            {"itens": [
               {"id": 1, "unidadeId": 1, "produtoId": 1, "nomeProduto": "Tapioca de Queijo Coalho", "quantidade": 98},
               {"id": 2, "unidadeId": 1, "produtoId": 2, "nomeProduto": "Cuscuz com Carne de Sol", "quantidade": 100}],
             "page": 1, "limit": 2, "totalItens": 7, "totalPaginas": 4}""";

    public static final String PEDIDO_APROVADO = """
            {"id": 10, "canalPedido": "APP", "status": "EM_PREPARO", "total": 25.00,
             "criadoEm": "2026-10-09T10:00:00",
             "itens": [{"produtoId": 1, "nomeProduto": "Tapioca de Queijo Coalho", "quantidade": 2,
                        "precoUnitario": 12.50, "subtotal": 25.00}],
             "pagamento": {"status": "APROVADO", "formaPagamento": "PIX"}}""";

    public static final String PEDIDO_RECUSADO = """
            {"id": 11, "canalPedido": "TOTEM", "status": "PAGAMENTO_RECUSADO", "total": 562.50,
             "criadoEm": "2026-10-09T10:05:00",
             "itens": [{"produtoId": 1, "nomeProduto": "Tapioca de Queijo Coalho", "quantidade": 45,
                        "precoUnitario": 12.50, "subtotal": 562.50}],
             "pagamento": {"status": "RECUSADO", "formaPagamento": "CARTAO"}}""";

    public static final String PEDIDO_PENDENTE = """
            {"id": 12, "canalPedido": "WEB", "status": "AGUARDANDO_PAGAMENTO", "total": 12.50,
             "criadoEm": "2026-10-09T10:10:00",
             "itens": [{"produtoId": 1, "nomeProduto": "Tapioca de Queijo Coalho", "quantidade": 1,
                        "precoUnitario": 12.50, "subtotal": 12.50}],
             "pagamento": {"status": "PENDENTE", "formaPagamento": "PAGAMENTO_INDISPONIVEL"}}""";

    public static final String PEDIDO_PRONTO = """
            {"id": 10, "canalPedido": "APP", "status": "PRONTO", "total": 25.00,
             "criadoEm": "2026-10-09T10:00:00",
             "itens": [{"produtoId": 1, "nomeProduto": "Tapioca de Queijo Coalho", "quantidade": 2,
                        "precoUnitario": 12.50, "subtotal": 25.00}],
             "pagamento": {"status": "APROVADO", "formaPagamento": "PIX"}}""";

    public static final String PAGINA_PEDIDOS = """
            {"itens": [
               {"id": 10, "canalPedido": "APP", "status": "EM_PREPARO", "total": 25.00,
                "criadoEm": "2026-10-09T10:00:00",
                "itens": [{"produtoId": 1, "nomeProduto": "Tapioca de Queijo Coalho", "quantidade": 2,
                           "precoUnitario": 12.50, "subtotal": 25.00}],
                "pagamento": {"status": "APROVADO", "formaPagamento": "PIX"}}],
             "page": 1, "limit": 10, "totalItens": 1, "totalPaginas": 1}""";

    public static final String PAGINA_AUDITORIA = """
            {"itens": [
               {"id": 21, "usuarioEmail": "atendente@raizes.com", "acao": "STATUS_ATUALIZADO", "entidade": "Pedido",
                "entidadeId": 10, "detalhes": "de=EM_PREPARO; para=PRONTO", "criadoEm": "2026-10-09T10:20:00"},
               {"id": 18, "usuarioEmail": "cliente@raizes.com", "acao": "PEDIDO_CRIADO", "entidade": "Pedido",
                "entidadeId": 10, "detalhes": "canal=APP; total=25.00; clienteId=4", "criadoEm": "2026-10-09T10:00:00"}],
             "page": 1, "limit": 10, "totalItens": 2, "totalPaginas": 1}""";

    public static final String RELATORIO_VENDAS = """
            {"inicio": "2026-10-01", "fim": "2026-10-31", "totalPedidos": 4, "faturamentoTotal": 98.70,
             "vendasPorUnidade": [
               {"unidadeId": 1, "unidade": "Raízes Recife Centro", "estado": "PE", "quantidadePedidos": 2, "faturamento": 62.80},
               {"unidadeId": 2, "unidade": "Raízes Quiosque Shopping", "estado": "PE", "quantidadePedidos": 1, "faturamento": 18.90},
               {"unidadeId": 3, "unidade": "Raízes Fortaleza Aldeota", "estado": "CE", "quantidadePedidos": 1, "faturamento": 17.00}],
             "vendasPorEstado": [
               {"estado": "PE", "quantidadePedidos": 3, "faturamento": 81.70},
               {"estado": "CE", "quantidadePedidos": 1, "faturamento": 17.00}],
             "produtosMaisVendidos": [
               {"produtoId": 2, "produto": "Cuscuz com Carne de Sol", "quantidadeVendida": 3, "faturamento": 56.70},
               {"produtoId": 1, "produto": "Tapioca de Queijo Coalho", "quantidadeVendida": 2, "faturamento": 25.00}]}""";

    // ---------------------------------------------------------------- erros (formato padrão)

    public static final String ERRO_401 = """
            {"error": "NAO_AUTENTICADO",
             "message": "Autenticação necessária. Envie um token válido no header Authorization.",
             "details": [], "timestamp": "2026-10-09T13:00:00Z", "path": "/pedidos",
             "requestId": "3f1c2a9e-7b4d-4c1a-9f2e-1a2b3c4d5e6f"}""";

    public static final String ERRO_CREDENCIAIS = """
            {"error": "CREDENCIAIS_INVALIDAS", "message": "E-mail ou senha inválidos.",
             "details": [], "timestamp": "2026-10-09T13:00:00Z", "path": "/auth/login",
             "requestId": "8a6b0c1d-2e3f-4a5b-8c7d-9e0f1a2b3c4d"}""";

    public static final String ERRO_403 = """
            {"error": "ACESSO_NEGADO", "message": "Seu perfil não possui permissão para esta operação.",
             "details": [], "timestamp": "2026-10-09T13:00:00Z", "path": "/relatorios/vendas",
             "requestId": "5c4d3e2f-1a0b-4c9d-8e7f-6a5b4c3d2e1f"}""";

    public static final String ERRO_403_PEDIDO_OUTRO_CLIENTE = """
            {"error": "ACESSO_NEGADO", "message": "Clientes só podem criar pedidos para si mesmos.",
             "details": [], "timestamp": "2026-10-09T13:00:00Z", "path": "/pedidos",
             "requestId": "0f1e2d3c-4b5a-4968-8776-5a4b3c2d1e0f"}""";

    public static final String ERRO_404 = """
            {"error": "RECURSO_NAO_ENCONTRADO", "message": "Pedido não encontrado: id 999",
             "details": [], "timestamp": "2026-10-09T13:00:00Z", "path": "/pedidos/999",
             "requestId": "9b8a7c6d-5e4f-4a3b-9c2d-1e0f9a8b7c6d"}""";

    public static final String ERRO_404_PRODUTO = """
            {"error": "RECURSO_NAO_ENCONTRADO", "message": "Produto não encontrado: id 999",
             "details": [], "timestamp": "2026-10-09T13:00:00Z", "path": "/pedidos",
             "requestId": "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d"}""";

    public static final String ERRO_404_UNIDADE = """
            {"error": "RECURSO_NAO_ENCONTRADO", "message": "Unidade não encontrada: id 99",
             "details": [], "timestamp": "2026-10-09T13:00:00Z", "path": "/unidades/99/cardapio",
             "requestId": "2b3c4d5e-6f7a-4b8c-9d0e-1f2a3b4c5d6e"}""";

    public static final String ERRO_422 = """
            {"error": "VALIDACAO_FALHOU", "message": "Um ou mais campos são inválidos.",
             "details": [{"field": "canalPedido", "issue": "não deve ser nulo"},
                         {"field": "itens[0].quantidade", "issue": "deve ser maior que ou igual à 1"}],
             "timestamp": "2026-10-09T13:00:00Z", "path": "/pedidos",
             "requestId": "4d5e6f7a-8b9c-4d0e-9f1a-2b3c4d5e6f7a"}""";

    public static final String ERRO_422_EMAIL = """
            {"error": "VALIDACAO_FALHOU", "message": "Um ou mais campos são inválidos.",
             "details": [{"field": "email", "issue": "deve ser um endereço de e-mail bem formado"}],
             "timestamp": "2026-10-09T13:00:00Z", "path": "/usuarios",
             "requestId": "6f7a8b9c-0d1e-4f2a-8b3c-4d5e6f7a8b9c"}""";

    public static final String ERRO_400_REQUISICAO = """
            {"error": "REQUISICAO_INVALIDA",
             "message": "Corpo da requisição malformado ou com valor não aceito (verifique enums como canalPedido).",
             "details": [], "timestamp": "2026-10-09T13:00:00Z", "path": "/pedidos",
             "requestId": "7a8b9c0d-1e2f-4a3b-8c4d-5e6f7a8b9c0d"}""";

    public static final String ERRO_400_PAGINACAO = """
            {"error": "PARAMETRO_INVALIDO", "message": "A página deve ser maior ou igual a 1.",
             "details": [{"field": "page", "issue": "A página deve ser maior ou igual a 1."}],
             "timestamp": "2026-10-09T13:00:00Z", "path": "/produtos",
             "requestId": "8b9c0d1e-2f3a-4b4c-9d5e-6f7a8b9c0d1e"}""";

    public static final String ERRO_400_FILTRO = """
            {"error": "PARAMETRO_INVALIDO", "message": "Parâmetro com valor ou formato inválido.",
             "details": [{"field": "status", "issue": "Valor 'XYZ' inválido. Valores aceitos: [AGUARDANDO_PAGAMENTO, EM_PREPARO, PRONTO, ENTREGUE, CANCELADO, PAGAMENTO_RECUSADO]"}],
             "timestamp": "2026-10-09T13:00:00Z", "path": "/pedidos",
             "requestId": "9c0d1e2f-3a4b-4c5d-8e6f-7a8b9c0d1e2f"}""";

    public static final String ERRO_400_PERIODO = """
            {"error": "PARAMETRO_INVALIDO", "message": "A data final deve ser igual ou posterior à data inicial.",
             "details": [{"field": "fim", "issue": "A data final deve ser igual ou posterior à data inicial."}],
             "timestamp": "2026-10-09T13:00:00Z", "path": "/relatorios/vendas",
             "requestId": "0d1e2f3a-4b5c-4d6e-9f7a-8b9c0d1e2f3a"}""";

    public static final String ERRO_400_PARAMETRO_AUSENTE = """
            {"error": "PARAMETRO_AUSENTE", "message": "Parâmetro obrigatório não informado.",
             "details": [{"field": "inicio", "issue": "Obrigatório"}],
             "timestamp": "2026-10-09T13:00:00Z", "path": "/relatorios/vendas",
             "requestId": "1e2f3a4b-5c6d-4e7f-8a9b-0c1d2e3f4a5b"}""";

    public static final String ERRO_409_ESTOQUE_PEDIDO = """
            {"error": "ESTOQUE_INSUFICIENTE", "message": "Estoque insuficiente para 'Tapioca de Queijo Coalho'.",
             "details": [{"field": "itens[0].quantidade", "issue": "Disponível: 100"}],
             "timestamp": "2026-10-09T13:00:00Z", "path": "/pedidos",
             "requestId": "2f3a4b5c-6d7e-4f8a-9b0c-1d2e3f4a5b6c"}""";

    public static final String ERRO_409_FORA_DE_EPOCA = """
            {"error": "PRODUTO_FORA_DE_EPOCA", "message": "O produto 'Canjica Junina' é sazonal e não está disponível hoje.",
             "details": [{"field": "itens[0].produtoId", "issue": "Disponível de 01/06/2026 a 31/07/2026"}],
             "timestamp": "2026-10-09T13:00:00Z", "path": "/pedidos",
             "requestId": "3a4b5c6d-7e8f-4a9b-8c0d-1e2f3a4b5c6d"}""";

    public static final String ERRO_409_CLIENTE_INVALIDO = """
            {"error": "CLIENTE_INVALIDO", "message": "O usuário informado não é um cliente ativo.",
             "details": [{"field": "clienteId", "issue": "Não é um cliente ativo"}],
             "timestamp": "2026-10-09T13:00:00Z", "path": "/pedidos",
             "requestId": "4b5c6d7e-8f9a-4b0c-9d1e-2f3a4b5c6d7e"}""";

    public static final String ERRO_409_ESTOQUE_MOVIMENTACAO = """
            {"error": "ESTOQUE_INSUFICIENTE", "message": "Estoque insuficiente para o produto Tapioca de Queijo Coalho. Disponível: 100",
             "details": [], "timestamp": "2026-10-09T13:00:00Z", "path": "/estoque/1/movimentar",
             "requestId": "5c6d7e8f-9a0b-4c1d-8e2f-3a4b5c6d7e8f"}""";

    public static final String ERRO_409_ESTOQUE_JA_EXISTE = """
            {"error": "ESTOQUE_JA_EXISTE", "message": "Já existe registro de estoque para este produto nesta unidade.",
             "details": [], "timestamp": "2026-10-09T13:00:00Z", "path": "/estoque",
             "requestId": "6d7e8f9a-0b1c-4d2e-9f3a-4b5c6d7e8f9a"}""";

    public static final String ERRO_409_UNIDADE_SEM_SUPORTE = """
            {"error": "UNIDADE_SEM_SUPORTE",
             "message": "Unidades do tipo REDUZIDA não podem oferecer produtos da categoria 'Pratos Quentes' (requer cozinha completa).",
             "details": [], "timestamp": "2026-10-09T13:00:00Z", "path": "/estoque",
             "requestId": "7e8f9a0b-1c2d-4e3f-8a4b-5c6d7e8f9a0b"}""";

    public static final String ERRO_409_TRANSICAO_INVALIDA = """
            {"error": "TRANSICAO_INVALIDA", "message": "Não é possível mudar o status de EM_PREPARO para ENTREGUE.",
             "details": [], "timestamp": "2026-10-09T13:00:00Z", "path": "/pedidos/10/status",
             "requestId": "8f9a0b1c-2d3e-4f4a-9b5c-6d7e8f9a0b1c"}""";

    public static final String ERRO_409_TRANSICAO_RESERVADA = """
            {"error": "TRANSICAO_RESERVADA_AO_SISTEMA",
             "message": "O status EM_PREPARO só pode ser definido automaticamente pelo retorno do gateway de pagamento.",
             "details": [], "timestamp": "2026-10-09T13:00:00Z", "path": "/pedidos/12/status",
             "requestId": "9a0b1c2d-3e4f-4a5b-8c6d-7e8f9a0b1c2d"}""";

    public static final String ERRO_409_PEDIDO_JA_PROCESSADO = """
            {"error": "PEDIDO_JA_PROCESSADO",
             "message": "Este pedido já teve o pagamento processado (status atual: EM_PREPARO). Não é possível retentar.",
             "details": [], "timestamp": "2026-10-09T13:00:00Z", "path": "/pedidos/12/pagamento/retentar",
             "requestId": "0b1c2d3e-4f5a-4b6c-9d7e-8f9a0b1c2d3e"}""";

    public static final String ERRO_409_EMAIL_JA_CADASTRADO = """
            {"error": "EMAIL_JA_CADASTRADO", "message": "Já existe um usuário cadastrado com este e-mail.",
             "details": [{"field": "email", "issue": "E-mail já cadastrado"}],
             "timestamp": "2026-10-09T13:00:00Z", "path": "/usuarios",
             "requestId": "1c2d3e4f-5a6b-4c7d-8e8f-9a0b1c2d3e4f"}""";
}
