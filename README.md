# Raízes do Nordeste — API Back-end

API REST da rede de lanchonetes Raízes do Nordeste, desenvolvida como atividade prática do Projeto Multidisciplinar (Trilha Back-end) do curso de Análise e Desenvolvimento de Sistemas da UNINTER.

Implementa o fluxo crítico **Pedido → Pagamento (mock) → Atualização de status**, com controle de estoque por unidade, rastreabilidade por canal de venda (`canalPedido`), autenticação JWT com perfis, auditoria de ações sensíveis e controles de LGPD.

| Item | Onde |
|---|---|
| Repositório | https://github.com/l-felipesp/API-Raizesdonordeste |
| Swagger UI (com a API rodando) | http://localhost:8080/swagger-ui.html |
| Contrato OpenAPI (JSON) | http://localhost:8080/v3/api-docs |
| Coleção Postman | [`postman/Raizes_do_Nordeste.postman_collection.json`](postman/Raizes_do_Nordeste.postman_collection.json) |
| Health check | http://localhost:8080/actuator/health |

## Sumário

1. [Tecnologias e requisitos](#1-tecnologias-e-requisitos)
2. [Configuração](#2-configuração)
3. [Banco de dados, migrations e seed](#3-banco-de-dados-migrations-e-seed)
4. [Como executar](#4-como-executar)
5. [Documentação da API (Swagger)](#5-documentação-da-api-swagger)
6. [Testes](#6-testes)
7. [Estrutura do projeto](#7-estrutura-do-projeto)
8. [Endpoints](#8-endpoints)
9. [Regras de negócio principais](#9-regras-de-negócio-principais)
10. [Segurança e LGPD](#10-segurança-e-lgpd)
11. [Escopo e decisões](#11-escopo-e-decisões)

## 1. Tecnologias e requisitos

| Componente | Versão |
|---|---|
| Java | 21 (LTS) |
| Spring Boot | 3.5.16 (Web, Data JPA, Security, Validation, Actuator) |
| PostgreSQL | 16 ou superior (testado no 16 e no 18) |
| Flyway | migrations versionadas (versão gerenciada pelo Spring Boot) |
| JWT | io.jsonwebtoken (jjwt) 0.12.6 |
| Documentação | springdoc-openapi 2.8.14 (Swagger UI) |
| Build | Maven Wrapper (incluso; não é preciso instalar o Maven) |
| Testes | JUnit 5 e Mockito (via `spring-boot-starter-test`) |

Para executar é preciso ter instalados o **JDK 21** e o **PostgreSQL**. As demais dependências são baixadas automaticamente pelo Maven Wrapper na primeira execução.

## 2. Configuração

As credenciais ficam fora do código, num arquivo `.env` na raiz do projeto, que **não** é versionado.

1. Copie o modelo:
   ```bash
   # Windows (PowerShell)
   Copy-Item .env.example .env
   # Linux / macOS
   cp .env.example .env
   ```
2. Edite o `.env` e preencha os valores:

| Variável | Obrigatória | Descrição |
|---|---|---|
| `DB_URL` | não | URL JDBC do banco. Padrão: `jdbc:postgresql://localhost:5432/raizes_nordeste` |
| `DB_USERNAME` | não | Usuário do PostgreSQL. Padrão: `postgres` |
| `DB_PASSWORD` | **sim** | Senha do PostgreSQL |
| `JWT_SECRET` | **sim** | Chave de assinatura dos tokens, com **no mínimo 32 caracteres** |
| `JWT_EXPIRATION_MS` | não | Validade do token em milissegundos. Padrão: `3600000` (1 hora) |
| `FLYWAY_LOCATIONS` | não | Ver a seção 3. Padrão: estrutura e dados de teste |

Sem `DB_PASSWORD` ou `JWT_SECRET`, a aplicação não inicia (falha intencional, para não rodar com segredos fracos).

Para gerar um `JWT_SECRET` aleatório:
```powershell
# Windows (PowerShell)
$b = New-Object byte[] 48; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
```
```bash
# Linux / macOS
openssl rand -base64 48
```

## 3. Banco de dados, migrations e seed

1. Crie o banco vazio (no pgAdmin ou no `psql`):
   ```sql
   CREATE DATABASE raizes_nordeste;
   ```
2. Pronto. As tabelas e os dados iniciais são criados **automaticamente pelo Flyway** quando a API inicia. O Hibernate apenas valida (`ddl-auto=validate`) se as entidades correspondem ao banco.

| Migration | Pasta | Conteúdo |
|---|---|---|
| `V1__criar_schema_inicial.sql` | `db/migration` | Tabelas, chaves, restrições e índices |
| `V2__seed_dados_iniciais.sql` | `db/seed` | Usuários, unidades, produtos e estoque de teste |
| `V3__lgpd_sazonalidade_e_indices.sql` | `db/migration` | Consentimento e anonimização (LGPD), produtos sazonais, índice dos relatórios |
| `V4__seed_produto_sazonal.sql` | `db/seed` | Produto sazonal de exemplo (período junino) |

**Usuários do seed** (senha `Senha@123` para todos):

| E-mail | Perfil |
|---|---|
| `admin@raizes.com` | ADMIN |
| `gerente@raizes.com` | GERENTE |
| `atendente@raizes.com` | ATENDENTE |
| `cliente@raizes.com` | CLIENTE |

O seed também cria três unidades (duas completas e uma reduzida), sete produtos e o estoque de cada unidade.

Os dados de teste ficam separados em `db/seed`. Em produção, para não carregá-los, defina no `.env`:
```
FLYWAY_LOCATIONS=classpath:db/migration
```

Para recriar o banco do zero, execute os comandos abaixo **um de cada vez**, conectado ao banco `postgres`, e inicie a API novamente:
```sql
DROP DATABASE raizes_nordeste WITH (FORCE);
CREATE DATABASE raizes_nordeste;
```

## 4. Como executar

```bash
# Windows (PowerShell)
.\mvnw spring-boot:run
# Linux / macOS
./mvnw spring-boot:run
```

A API sobe em `http://localhost:8080`. No primeiro início, o log deve mostrar a aplicação das migrations:
```
Successfully applied 4 migrations to schema "public", now at version v4
```

Para conferir se a API e o banco estão no ar: `GET http://localhost:8080/actuator/health` deve responder `{"status":"UP"}`.

## 5. Documentação da API (Swagger)

Com a API rodando, acesse **http://localhost:8080/swagger-ui.html**.

Cada endpoint traz descrição, perfis permitidos, parâmetros, exemplos de request e response e todos os códigos de status possíveis, com o respectivo erro padronizado.

Para testar endpoints protegidos (com cadeado):
1. Execute `POST /auth/login` com um dos usuários do seed.
2. Copie o `accessToken` da resposta.
3. Clique em **Authorize** e cole o token (sem a palavra `Bearer`).

## 6. Testes

### 6.1 Testes automatizados (JUnit e Mockito)

São 42 testes unitários das regras de negócio e do fluxo crítico, que **não precisam de banco de dados**:
```bash
# Windows (PowerShell) - as aspas são necessárias
.\mvnw test "-Dtest=!BackendApplicationTests"
# Linux / macOS
./mvnw test '-Dtest=!BackendApplicationTests'
```

Para rodar todos os testes, incluindo o de integração que sobe a aplicação completa (exige o PostgreSQL rodando e o `.env` configurado):
```bash
.\mvnw test     # Windows
./mvnw test     # Linux / macOS
```

| Classe de teste | O que valida |
|---|---|
| `PedidoServiceTest` | Fluxo crítico: pagamento aprovado, recusado e com falha do gateway; nova tentativa; estoque insuficiente; produto fora de época; pedido em nome de cliente; acesso por dono do pedido |
| `UsuarioServiceTest` | LGPD: consentimento, revogação, e-mail duplicado e anonimização |
| `StatusPedidoTest` | Transições permitidas e bloqueadas do status do pedido |
| `PedidoTest`, `EstoqueTest`, `ProdutoTest`, `UsuarioTest` | Regras do domínio |
| `PagamentoGatewayMockServiceTest` | As três respostas do gateway de pagamento simulado |
| `PaginacaoTest` | Validação de `page`, `limit` e `sort` |

### 6.2 Coleção Postman (testes da API em execução)

A coleção [`postman/Raizes_do_Nordeste.postman_collection.json`](postman/Raizes_do_Nordeste.postman_collection.json) contém **72 cenários** (37 positivos e 35 negativos), todos com verificações automáticas, organizados em 9 pastas:

| Pasta | Cenários |
|---|---|
| 01 - Auth e preparação | T01 a T07 |
| 02 - Usuários e cadastro | T08 a T12 |
| 03 - Unidades e cardápio | T13 a T16 |
| 04 - Produtos e estoque | T17 a T27 |
| 05 - Pedidos (fluxo crítico) | T28 a T41 |
| 06 - Pagamento mock e status | T42 a T53 |
| 07 - Auditoria e relatórios | T54 a T61 |
| 08 - LGPD (direitos do titular) | T62 a T67 |
| 09 - Erros e disponibilidade | T68 a T72 |

Como executar:
1. Com a API rodando e o seed aplicado, importe o arquivo no Postman (**Import**).
2. Clique nos três pontos da coleção → **Run collection** → **Run**, mantendo a ordem das pastas.

Não há passo manual: a coleção usa os usuários do seed e cria, a cada execução, o próprio cliente, produto e estoque. Por isso ela pode ser executada várias vezes seguidas. A URL da API fica na variável `baseUrl` da coleção (padrão `http://localhost:8080`).

## 7. Estrutura do projeto

```
src/main/java/com/raizesdonordeste/backend/
├── domain/           Entidades, enums e exceções de negócio (regras encapsuladas no domínio)
├── application/      Serviços que orquestram os casos de uso
├── infrastructure/   Persistência (repositórios JPA), segurança (JWT), pagamento mock e configuração
└── api/              Controllers, DTOs, paginação, documentação OpenAPI e tratamento de erros
src/main/resources/
├── application.properties
└── db/
    ├── migration/    Estrutura do banco (Flyway)
    └── seed/         Dados de teste (Flyway)
src/test/java/        Testes automatizados
postman/              Coleção de testes da API
```

## 8. Endpoints

| Recurso | Método e rota | Acesso |
|---|---|---|
| Autenticação | `POST /auth/login` | Público |
| Usuários | `POST /usuarios` | Público |
| | `GET /usuarios/me` | Autenticado |
| | `PATCH /usuarios/me/consentimento` | CLIENTE |
| | `DELETE /usuarios/me` | CLIENTE |
| Unidades | `GET /unidades`, `GET /unidades/{id}`, `GET /unidades/{id}/cardapio` | Público |
| | `POST /unidades` | GERENTE, ADMIN |
| Produtos | `GET /produtos`, `GET /produtos/{id}` | Público |
| | `POST /produtos`, `PUT /produtos/{id}` | GERENTE, ADMIN |
| Estoque | `GET /estoque?unidadeId=`, `PATCH /estoque/{id}/movimentar` | ATENDENTE, GERENTE, ADMIN |
| | `POST /estoque` | GERENTE, ADMIN |
| Pedidos | `POST /pedidos` | CLIENTE, ATENDENTE |
| | `GET /pedidos`, `GET /pedidos/{id}` | Autenticado (CLIENTE vê só os próprios) |
| | `PATCH /pedidos/{id}/status` | ATENDENTE, GERENTE, ADMIN |
| | `POST /pedidos/{id}/pagamento/retentar` | CLIENTE, ATENDENTE |
| Auditoria | `GET /auditoria?entidade=&entidadeId=` | GERENTE, ADMIN |
| Relatórios | `GET /relatorios/vendas?inicio=&fim=` | GERENTE, ADMIN |
| Disponibilidade | `GET /actuator/health` | Público |

Listagens são paginadas com `page` (a partir de 1), `limit` (1 a 100) e `sort` (`campo` ou `campo,asc|desc`). `GET /pedidos` aceita os filtros `canalPedido` e `status`.

Todas as falhas seguem o mesmo formato:
```json
{
  "error": "ESTOQUE_INSUFICIENTE",
  "message": "Estoque insuficiente para 'Tapioca de Queijo Coalho'.",
  "details": [ { "field": "itens[0].quantidade", "issue": "Disponível: 100" } ],
  "timestamp": "2026-10-09T13:00:00Z",
  "path": "/pedidos",
  "requestId": "2f3a4b5c-6d7e-4f8a-9b0c-1d2e3f4a5b6c"
}
```

## 9. Regras de negócio principais

- **Multicanalidade:** todo pedido exige `canalPedido` (`APP`, `TOTEM`, `BALCAO`, `PICKUP`, `WEB`), e a listagem filtra por canal.
- **Total calculado no servidor:** o preço vem sempre do cadastro do produto, nunca do request.
- **Estoque por unidade:** reservado ao criar o pedido e devolvido se o pagamento for recusado ou falhar. A saída nunca deixa o saldo negativo.
- **Pagamento mock (determinístico):** total a partir de R$ 500,00 é recusado; `formaPagamento` igual a `PAGAMENTO_INDISPONIVEL` simula falha de comunicação com o gateway (o pedido fica aguardando e aceita nova tentativa, sem duplicar); nos demais casos o pagamento é aprovado.
- **Status do pedido:** `AGUARDANDO_PAGAMENTO → EM_PREPARO | PAGAMENTO_RECUSADO | CANCELADO`; `EM_PREPARO → PRONTO | CANCELADO`; `PRONTO → ENTREGUE | CANCELADO`. `EM_PREPARO` e `PAGAMENTO_RECUSADO` são definidos apenas pelo retorno do pagamento.
- **Unidades reduzidas** não oferecem produtos da categoria Pratos Quentes.
- **Produtos sazonais** (ex.: período junino) só aparecem no cardápio e podem ser pedidos dentro do período de disponibilidade.
- **Relatórios da matriz:** vendas por unidade, por estado e produtos mais vendidos, considerando apenas pagamentos aprovados de pedidos não cancelados.

## 10. Segurança e LGPD

- Senhas armazenadas com hash BCrypt; autenticação stateless com JWT; autorização por perfil em cada endpoint.
- Respostas nunca expõem hash de senha. O e-mail aparece apenas para o próprio titular (`GET /usuarios/me`) e, na consulta de auditoria, para os perfis GERENTE e ADMIN, como identificação de quem executou cada ação.
- Um cliente não consegue ver nem agir sobre pedidos de outro cliente.
- Consentimento para fidelização opcional, registrado com data e hora, e revogável a qualquer momento.
- O titular pode consultar os próprios dados (cada acesso é auditado) e pedir a exclusão: o cadastro é anonimizado e o e-mail é substituído por um pseudônimo também no histórico de auditoria, enquanto os pedidos são mantidos por obrigação fiscal.
- Auditoria das ações sensíveis: criação e cancelamento de pedidos, mudanças de status, pagamentos, movimentações de estoque, acesso a dados pessoais, consentimento e anonimização.
- Credenciais fora do código, em variáveis de ambiente.

## 11. Escopo e decisões

- **Implementado:** autenticação e perfis, unidades, cardápio por unidade, produtos (inclusive sazonais), estoque, pedidos com o fluxo completo de pagamento mock e status, auditoria, relatórios e controles de LGPD.
- **Mantido em nível conceitual (documentado no trabalho):** programa de fidelização (pontos e resgate) e promoções/campanhas. O consentimento para fidelização já é coletado e registrado.
- **Ator Cozinha:** representado conceitualmente; na implementação, as ações da cozinha (marcar pedido como pronto) são executadas pelo perfil ATENDENTE.
- **Sessão:** o token JWT expira em 1 hora. Renovação (refresh token) e revogação (logout no servidor) ficam como evolução futura.

---

## Documentação adicional

| Item | Arquivo |
|---|---|
| Contrato OpenAPI (cópia versionada) | [`docs/openapi.yaml`](docs/openapi.yaml) |
| DER | [`docs/diagramas/der.png`](docs/diagramas/der.png) |
| Diagrama de casos de uso | [`docs/diagramas/casos-de-uso.png`](docs/diagramas/casos-de-uso.png) |
| Diagrama de classes | [`docs/diagramas/classes.png`](docs/diagramas/classes.png) |
| Diagrama de sequência | [`docs/diagramas/sequencia.png`](docs/diagramas/sequencia.png) |
| Diagrama de estados do pedido | [`docs/diagramas/estados.png`](docs/diagramas/estados.png) |

Para ver o contrato no Swagger sem rodar a API, abra https://editor.swagger.io, vá em **File → Import URL** e cole:
`https://raw.githubusercontent.com/l-felipesp/API-Raizesdonordeste/main/docs/openapi.yaml`

Luiz Felipe Silva de Paula — RU 4930873
