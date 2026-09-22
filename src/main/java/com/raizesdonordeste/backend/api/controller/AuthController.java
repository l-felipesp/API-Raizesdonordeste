package com.raizesdonordeste.backend.api.controller;

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