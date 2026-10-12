package br.com.fiap.comed.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Converte as exceções da API em respostas RFC 9457 (ProblemDetail). As exceções padrão do
 * Spring MVC (405, parâmetro ausente etc.) já saem como ProblemDetail pela classe base.
 */
@RestControllerAdvice
public class TratadorDeErros extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(RecursoNaoEncontradoException e) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", e.getMessage());
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ProblemDetail duplicado(DuplicateKeyException e) {
        return problema(HttpStatus.CONFLICT, "Registro duplicado",
                "Já existe um registro com esse valor único (ex.: CRM).");
    }

    @ExceptionHandler(ConflitoException.class)
    public ProblemDetail conflito(ConflitoException e) {
        return problema(HttpStatus.CONFLICT, "Conflito", e.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (FieldError erro : e.getBindingResult().getFieldErrors()) {
            erros.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        ProblemDetail problema = problema(HttpStatus.BAD_REQUEST, "Dados inválidos",
                "Um ou mais campos não passaram na validação.");
        problema.setProperty("erros", erros);
        return ResponseEntity.badRequest().body(problema);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        return ResponseEntity.badRequest().body(problema(HttpStatus.BAD_REQUEST, "Corpo da requisição inválido",
                "JSON malformado ou com valor fora do domínio (ex.: enum desconhecido)."));
    }

    private static ProblemDetail problema(HttpStatus status, String titulo, String detalhe) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setTitle(titulo);
        return problema;
    }
}
