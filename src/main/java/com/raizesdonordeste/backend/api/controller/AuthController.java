package com.raizesdonordeste.backend.api.controller;

import com.raizesdonordeste.backend.api.doc.ExemplosOpenApi;
import com.raizesdonordeste.backend.api.dto.ErroResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import com.raizesdonordeste.backend.api.dto.LoginRequest;
import com.raizesdonordeste.backend.api.dto.LoginResponse;
import com.raizesdonordeste.backend.api.dto.UsuarioResumoDTO;
import com.raizesdonordeste.backend.domain.model.Usuario;
import com.raizesdonordeste.backend.infrastructure.security.JwtService;
import com.raizesdonordeste.backend.infrastructure.security.UsuarioDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Autenticação", description = "Login e emissão de token JWT")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Operation(summary = "Autenticar e obter token JWT",
            description = "Público. Devolve o accessToken a ser informado no botão Authorize (sem a palavra Bearer). Usuários do seed: admin@raizes.com, gerente@raizes.com, atendente@raizes.com e cliente@raizes.com, todos com a senha Senha@123.")
    @SecurityRequirements
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = LoginResponse.class), examples = @ExampleObject(name = "Login", value = ExemplosOpenApi.LOGIN_OK))),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Credenciais inválidas", value = ExemplosOpenApi.ERRO_CREDENCIAIS))),
            @ApiResponse(responseCode = "422", description = "E-mail ou senha ausentes ou malformados",
                    content = @Content(mediaType = ExemplosOpenApi.JSON, schema = @Schema(implementation = ErroResponse.class), examples = @ExampleObject(name = "Validação", value = ExemplosOpenApi.ERRO_422_EMAIL)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha())
        );

        UsuarioDetailsImpl userDetails = (UsuarioDetailsImpl) authentication.getPrincipal();
        Usuario usuario = userDetails.getUsuario();
        String token = jwtService.gerarToken(userDetails);

        LoginResponse response = new LoginResponse(
                token, "Bearer", expirationMs / 1000, UsuarioResumoDTO.from(usuario)
        );
        return ResponseEntity.ok(response);
    }
}