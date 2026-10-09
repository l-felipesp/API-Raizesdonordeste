package com.raizesdonordeste.backend.api.controller;

import com.raizesdonordeste.backend.api.doc.ExemplosOpenApi;
import com.raizesdonordeste.backend.api.dto.ErroResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;

import com.raizesdonordeste.backend.api.dto.RelatorioVendasResponse;
import com.raizesdonordeste.backend.api.exception.ParametroInvalidoException;
import com.raizesdonordeste.backend.application.service.RelatorioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Tag(name = "Relatórios", description = "Indicadores de vendas para a matriz")
@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    private static final long PERIODO_MAXIMO_DIAS = 366;

    private final RelatorioService relatorioService;

    public RelatorioController(RelatorioService relatorioService) {
        this.relatorioService = relatorioService;
    }

    @Operation(summary = "Relatório de vendas para a matriz",
            description = "Perfis GERENTE ou ADMIN. Considera apenas vendas efetivas: pagamento aprovado e pedido não cancelado. Datas inclusivas no formato yyyy-MM-dd, período máximo de 366 dias. A região corresponde ao estado (UF) da unidade.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Relatório gerado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = RelatorioVendasResponse.class), examples = @ExampleObject(name = "Outubro/2026", value = ExemplosOpenApi.RELATORIO_VENDAS))),
            @ApiResponse(responseCode = "400", description = "Período inválido ou parâmetro ausente",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = {
                            @ExampleObject(name = "Período invertido", value = ExemplosOpenApi.ERRO_400_PERIODO),
                            @ExampleObject(name = "Parâmetro ausente", value = ExemplosOpenApi.ERRO_400_PARAMETRO_AUSENTE)})),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Sem token", value = ExemplosOpenApi.ERRO_401))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão para a operação",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Perfil sem permissão", value = ExemplosOpenApi.ERRO_403)))
    })
    @GetMapping("/vendas")
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public RelatorioVendasResponse vendas(@Parameter(description = "Data inicial, inclusiva (yyyy-MM-dd)", example = "2026-10-01") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                          @Parameter(description = "Data final, inclusiva (yyyy-MM-dd)", example = "2026-10-31") @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        if (fim.isBefore(inicio)) {
            throw new ParametroInvalidoException("fim", "A data final deve ser igual ou posterior à data inicial.");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) >= PERIODO_MAXIMO_DIAS) {
            throw new ParametroInvalidoException("fim", "O período máximo do relatório é de " + PERIODO_MAXIMO_DIAS + " dias.");
        }
        return relatorioService.gerarRelatorioVendas(inicio, fim);
    }
}
