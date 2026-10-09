package com.raizesdonordeste.backend.api.exception;

import com.raizesdonordeste.backend.api.dto.ErroResponse;
import com.raizesdonordeste.backend.domain.exception.AcessoNegadoException;
import com.raizesdonordeste.backend.domain.exception.RecursoNaoEncontradoException;
import com.raizesdonordeste.backend.domain.exception.RegraDeNegocioException;
import com.raizesdonordeste.backend.infrastructure.payment.GatewayIndisponivelException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import java.util.Arrays;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErroResponse> parametroAusente(MissingServletRequestParameterException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                ErroResponse.de("PARAMETRO_AUSENTE", "Parâmetro obrigatório não informado.", req.getRequestURI(),
                        List.of(new ErroResponse.CampoInvalido(ex.getParameterName(), "Obrigatório"))));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErroResponse> parametroComTipoInvalido(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String problema = "Valor '" + ex.getValue() + "' inválido";
        Class<?> tipo = ex.getRequiredType();
        if (tipo != null && tipo.isEnum()) {
            problema += ". Valores aceitos: " + Arrays.toString(tipo.getEnumConstants());
        }
        return ResponseEntity.badRequest().body(
                ErroResponse.de("PARAMETRO_INVALIDO", "Parâmetro com valor ou formato inválido.", req.getRequestURI(),
                        List.of(new ErroResponse.CampoInvalido(ex.getName(), problema))));
    }

    @ExceptionHandler(ParametroInvalidoException.class)
    public ResponseEntity<ErroResponse> parametroInvalido(ParametroInvalidoException ex, HttpServletRequest req) {
        return ResponseEntity.badRequest().body(
                ErroResponse.de("PARAMETRO_INVALIDO", ex.getMessage(), req.getRequestURI(),
                        List.of(new ErroResponse.CampoInvalido(ex.getCampo(), ex.getMessage()))));
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

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<ErroResponse> acessoNegadoPorRegra(AcessoNegadoException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErroResponse.de("ACESSO_NEGADO", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErroResponse.de("RECURSO_NAO_ENCONTRADO", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErroResponse> rotaInexistente(NoResourceFoundException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErroResponse.de("RECURSO_NAO_ENCONTRADO", "Rota não encontrada.", req.getRequestURI()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErroResponse> metodoNaoPermitido(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
                ErroResponse.de("METODO_NAO_PERMITIDO",
                        "O método " + ex.getMethod() + " não é suportado nesta rota.", req.getRequestURI()));
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<ErroResponse> regraDeNegocio(RegraDeNegocioException ex, HttpServletRequest req) {
        List<ErroResponse.CampoInvalido> details = ex.getCampo() == null
                ? List.of()
                : List.of(new ErroResponse.CampoInvalido(ex.getCampo(), ex.getProblema()));
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErroResponse.de(ex.getCodigoErro(), ex.getMessage(), req.getRequestURI(), details));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErroResponse> estadoInvalido(IllegalStateException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErroResponse.de("CONFLITO", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(GatewayIndisponivelException.class)
    public ResponseEntity<ErroResponse> gatewayIndisponivel(GatewayIndisponivelException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(
                ErroResponse.de("GATEWAY_INDISPONIVEL", ex.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResponse> integridade(DataIntegrityViolationException ex, HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                ErroResponse.de("VIOLACAO_DE_INTEGRIDADE",
                        "A operação viola uma regra de integridade dos dados (registro duplicado ou valor não permitido).",
                        req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> inesperado(Exception ex, HttpServletRequest req) {
        ErroResponse erro = ErroResponse.de("ERRO_INTERNO",
                "Ocorreu um erro inesperado. Contate o suporte informando o requestId.",
                req.getRequestURI());
        log.error("Erro inesperado [requestId={}] em {}", erro.requestId(), req.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }
}
