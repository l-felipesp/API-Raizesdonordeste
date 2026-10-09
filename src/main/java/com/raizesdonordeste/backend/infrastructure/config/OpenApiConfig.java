package com.raizesdonordeste.backend.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    private static final String DESCRICAO = """
            API REST do fluxo de pedidos, estoque e pagamento mock da rede Raízes do Nordeste. \
            Trilha Back-end — Projeto Multidisciplinar.

            **Como autenticar:** chame `POST /auth/login`, copie o `accessToken` da resposta, clique em \
            **Authorize** e cole o token (sem a palavra Bearer). Endpoints sem cadeado são públicos.

            **Usuários do seed** (senha `Senha@123` para todos): `admin@raizes.com` (ADMIN), \
            `gerente@raizes.com` (GERENTE), `atendente@raizes.com` (ATENDENTE) e `cliente@raizes.com` (CLIENTE).

            **Pagamento mock:** total a partir de R$ 500,00 é recusado; `formaPagamento` igual a \
            `PAGAMENTO_INDISPONIVEL` simula falha de comunicação com o gateway; os demais casos são aprovados.

            **Listagens:** paginadas com `page` (a partir de 1), `limit` (1 a 100) e `sort` (`campo` ou `campo,asc|desc`).

            **Erros:** todas as falhas seguem o mesmo formato JSON, com `error`, `message`, `details`, \
            `timestamp`, `path` e `requestId`.
            """;

    @Bean
    public OpenAPI raizesDoNordesteOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Raízes do Nordeste — API Back-end")
                        .description(DESCRICAO)
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}