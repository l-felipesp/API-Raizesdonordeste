package com.raizesdonordeste.backend.api.exception;

import com.raizesdonordeste.backend.api.dto.ErroResponse;
import com.raizesdonordeste.backend.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.backend.domain.exception.RegraDeNegocioException;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException ex, HttpServletRequest req) {
        List<ErroResponse.CampoInvalido> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> new ErroResponse.CampoInvalido(fe.getField(), fe.getDefaultMessage()))
                .toList();

        return ResponseEntity.unprocessableEntity().body(
                ErroResponse.de("VALIDACAO_FALHOU", "Um ou mais campos são inválidos.",
                        req.getRequestURI(), details));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResponse> corpoInvalido(HttpMessageNotReadableException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                ErroResponse.de("REQUISICAO_INVALIDA",
                        "Corpo da requisição malformado ou com valor não aceito (verifique enums como canalPedido).",
                        req.getRequestURI()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErroResponse> credenciais(BadCredentialsException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErroResponse.de("CREDENCIAIS_INVALIDAS", "E-mail ou senha inválidos.", req.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErroResponse> acessoNegado(AccessDeniedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErroResponse.de("ACESSO_NEGADO",
                        "Seu perfil não possui permissão para esta operação.", req.getRequestURI()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErroResponse.de("RECURSO_NAO_ENCONTRADO", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> regraDeNegocio(RegraDeNegocioException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErroResponse.de(ex.getCodigoErro(), ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErroResponse> estadoInvalido(IllegalStateException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErroResponse.de("CONFLITO", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErroResponse.de("ERRO_INTERNO",
                        "Ocorreu um erro inesperado. Contate o suporte informando o requestId.",
                        req.getRequestURI()));
    }
}