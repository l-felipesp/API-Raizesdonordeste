package com.raizesdonordeste.backend.api.controller;

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

    @GetMapping("/vendas")
    @PreAuthorize("hasRole('GERENTE') or hasRole('ADMIN')")
    public RelatorioVendasResponse vendas(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        if (fim.isBefore(inicio)) {
            throw new ParametroInvalidoException("fim", "A data final deve ser igual ou posterior à data inicial.");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) >= PERIODO_MAXIMO_DIAS) {
            throw new ParametroInvalidoException("fim", "O período máximo do relatório é de " + PERIODO_MAXIMO_DIAS + " dias.");
        }
        return relatorioService.gerarRelatorioVendas(inicio, fim);
    }
}
