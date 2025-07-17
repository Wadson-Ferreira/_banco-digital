package br.com.cdb.bancodigital.controller.exception;

import br.com.cdb.bancodigital.service.exception.BusinessException;
import br.com.cdb.bancodigital.service.exception.DataIntegrityException;
import br.com.cdb.bancodigital.service.exception.ObjectNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class ResourceExceptionHandler {
    // Manipulador para exceções de "Objeto Não Encontrado"
    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<StandardError> objectNotFound(ObjectNotFoundException e, HttpServletRequest request) {
        StandardError err = new StandardError(Instant.now(), HttpStatus.NOT_FOUND.value(), "Não Encontrado", e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }

    // Manipulador para exceções de violação de integridade de dados ou regras de negócio
    @ExceptionHandler({DataIntegrityException.class, BusinessException.class})
    public ResponseEntity<StandardError> dataIntegrity(RuntimeException e, HttpServletRequest request) {
        StandardError err = new StandardError(Instant.now(), HttpStatus.BAD_REQUEST.value(), "Requisição Inválida", e.getMessage(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }

    // Manipulador para erros de validação dos DTOs
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationError> validation(MethodArgumentNotValidException e, HttpServletRequest request) {
        ValidationError err = new ValidationError(Instant.now(), HttpStatus.UNPROCESSABLE_ENTITY.value(), "Erro de Validação", "Um ou mais campos estão inválidos.", request.getRequestURI());
        for (FieldError x : e.getBindingResult().getFieldErrors()) {
            err.addError(x.getField(), x.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(err);
    }
}
