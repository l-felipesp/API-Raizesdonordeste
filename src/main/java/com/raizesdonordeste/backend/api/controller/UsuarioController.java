package com.raizesdonordeste.backend.api.controller;

import com.raizesdonordeste.backend.api.dto.CadastroUsuarioRequest;
import com.raizesdonordeste.backend.api.dto.UsuarioResumoDTO;
import com.raizesdonordeste.backend.domain.enums.Perfil;
import com.raizesdonordeste.backend.domain.model.Usuario;
import com.raizesdonordeste.backend.infrastructure.persistence.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<UsuarioResumoDTO> cadastrar(@Valid @RequestBody CadastroUsuarioRequest request) {
        if (usuarioRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("Já existe um usuário cadastrado com este e-mail.");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .perfil(Perfil.CLIENTE)
                .build();

        usuario = usuarioRepository.save(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResumoDTO.from(usuario));
    }
}