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
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

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

    @Operation(summary = "Cadastrar cliente",
            description = "Público. Cria um usuário com perfil CLIENTE. aceitaFidelizacao é opcional (LGPD: consentimento livre); quando informado, a decisão é registrada com data e hora.")
    @SecurityRequirements
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente cadastrado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = UsuarioResumoDTO.class), examples = @ExampleObject(name = "Cadastro", value = ExemplosOpenApi.USUARIO_CRIADO))),
            @ApiResponse(responseCode = "400", description = "Corpo malformado ou valor de enum inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "JSON inválido", value = ExemplosOpenApi.ERRO_400_REQUISICAO))),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "E-mail duplicado", value = ExemplosOpenApi.ERRO_409_EMAIL_JA_CADASTRADO))),
            @ApiResponse(responseCode = "422", description = "Campos obrigatórios ausentes ou com formato inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "E-mail inválido", value = ExemplosOpenApi.ERRO_422_EMAIL)))
    })
    @PostMapping
    public ResponseEntity<UsuarioResumoDTO> cadastrar(@Valid @RequestBody CadastroUsuarioRequest request) {
        var usuario = usuarioService.cadastrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResumoDTO.from(usuario));
    }

    @Operation(summary = "Consultar meus dados (titular)",
            description = "Qualquer perfil autenticado. Retorna os dados pessoais do próprio usuário. Cada acesso é registrado na auditoria como DADOS_PESSOAIS_ACESSADOS.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados do titular",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = UsuarioDetalheResponse.class), examples = @ExampleObject(name = "Meus dados", value = ExemplosOpenApi.USUARIO_ME))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Sem token", value = ExemplosOpenApi.ERRO_401)))
    })
    @GetMapping("/me")
    public UsuarioDetalheResponse meusDados(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioDetailsImpl usuarioLogado) {
        return UsuarioDetalheResponse.from(usuarioService.consultarProprioCadastro(usuarioLogado.getUsuario().getId()));
    }

    @Operation(summary = "Conceder ou revogar o consentimento de fidelização",
            description = "Perfil CLIENTE. Registra a decisão do titular com data e hora e gera CONSENTIMENTO_CONCEDIDO ou CONSENTIMENTO_REVOGADO na auditoria.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Consentimento atualizado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = UsuarioDetalheResponse.class), examples = @ExampleObject(name = "Meus dados", value = ExemplosOpenApi.USUARIO_ME))),
            @ApiResponse(responseCode = "400", description = "Corpo malformado ou valor de enum inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "JSON inválido", value = ExemplosOpenApi.ERRO_400_REQUISICAO))),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Sem token", value = ExemplosOpenApi.ERRO_401))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão para a operação",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Perfil sem permissão", value = ExemplosOpenApi.ERRO_403))),
            @ApiResponse(responseCode = "422", description = "Campos obrigatórios ausentes ou com formato inválido",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Validação", value = ExemplosOpenApi.ERRO_422)))
    })
    @PatchMapping("/me/consentimento")
    @PreAuthorize("hasRole('CLIENTE')")
    public UsuarioDetalheResponse atualizarConsentimento(@Valid @RequestBody ConsentimentoRequest request,
                                                         @Parameter(hidden = true) @AuthenticationPrincipal UsuarioDetailsImpl usuarioLogado) {
        return UsuarioDetalheResponse.from(
                usuarioService.atualizarConsentimento(usuarioLogado.getUsuario().getId(), request.aceitaFidelizacao()));
    }

    @Operation(summary = "Excluir meus dados (anonimização)",
            description = "Perfil CLIENTE. Remove nome e e-mail do cadastro e invalida a senha, impedindo novos logins. Os pedidos são mantidos (obrigação fiscal e relatórios) e o e-mail no histórico de auditoria é trocado por um pseudônimo.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dados pessoais anonimizados"),
            @ApiResponse(responseCode = "401", description = "Token ausente, inválido ou expirado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Sem token", value = ExemplosOpenApi.ERRO_401))),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão para a operação",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Perfil sem permissão", value = ExemplosOpenApi.ERRO_403)))
    })
    @DeleteMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    public ResponseEntity<Void> excluirMeusDados(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioDetailsImpl usuarioLogado) {
        usuarioService.anonimizar(usuarioLogado.getUsuario().getId());
        return ResponseEntity.noContent().build();
    }
}
