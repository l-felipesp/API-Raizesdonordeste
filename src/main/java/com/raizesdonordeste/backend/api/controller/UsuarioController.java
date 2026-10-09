package com.raizesdonordeste.backend.api.controller;

import com.raizesdonordeste.backend.api.dto.CadastroUsuarioRequest;
import com.raizesdonordeste.backend.api.dto.ConsentimentoRequest;
import com.raizesdonordeste.backend.api.dto.UsuarioDetalheResponse;
import com.raizesdonordeste.backend.api.dto.UsuarioResumoDTO;
import com.raizesdonordeste.backend.application.service.UsuarioService;
import com.raizesdonordeste.backend.infrastructure.security.UsuarioDetailsImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Usuários", description = "Cadastro de clientes e direitos do titular (LGPD)")
@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping
    public ResponseEntity<UsuarioResumoDTO> cadastrar(@Valid @RequestBody CadastroUsuarioRequest request) {
        var usuario = usuarioService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResumoDTO.from(usuario));
    }

    @GetMapping("/me")
    public UsuarioDetalheResponse meusDados(@AuthenticationPrincipal UsuarioDetailsImpl usuarioLogado) {
        return UsuarioDetalheResponse.from(usuarioService.consultarProprioCadastro(usuarioLogado.getUsuario().getId()));
    }

    @PatchMapping("/me/consentimento")
    @PreAuthorize("hasRole('CLIENTE')")
    public UsuarioDetalheResponse atualizarConsentimento(@Valid @RequestBody ConsentimentoRequest request,
                                                         @AuthenticationPrincipal UsuarioDetailsImpl usuarioLogado) {
        return UsuarioDetalheResponse.from(
                usuarioService.atualizarConsentimento(usuarioLogado.getUsuario().getId(), request.aceitaFidelizacao()));
    }

    @DeleteMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Void> excluirMeusDados(@AuthenticationPrincipal UsuarioDetailsImpl usuarioLogado) {
        usuarioService.anonimizar(usuarioLogado.getUsuario().getId());
        return ResponseEntity.noContent().build();
    }
}
