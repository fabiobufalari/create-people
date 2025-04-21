package com.bufalari.createpeople.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import com.bufalari.createpeople.exception.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler para tratar exceções da aplicação e retornar respostas padronizadas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Tratamento de erros de validação (bean validation) – retorna HTTP 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        // Loga o erro com detalhes (opcional)
        logger.error("Erro de validação: {}", ex.getMessage());

        // Construir mensagem detalhada com os erros de validação em cada campo
        StringBuilder errors = new StringBuilder();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.append(fieldError.getField())
                  .append(": ")
                  .append(fieldError.getDefaultMessage())
                  .append("; ");
        }
        String errorMessage = "Erro de validação: " + errors.toString();
        if (errorMessage.endsWith("; ")) {
            errorMessage = errorMessage.substring(0, errorMessage.length() - 2);
        }

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse body = new ErrorResponse(
            LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            errorMessage,
            request.getRequestURI(),
            "Por favor, corrija os campos inválidos e tente novamente."
        );
        return ResponseEntity.status(status).body(body);
    }

    /**
     * Tratamento genérico para exceções não tratadas explicitamente – retorna HTTP 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        // Loga o erro inesperado (opcionalmente pode incluir um ID de rastreamento)
        logger.error("Erro inesperado no servidor: {}", ex.getMessage(), ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse body = new ErrorResponse(
            LocalDateTime.now(),
            status.value(),
            status.getReasonPhrase(),
            ex.getMessage(),
            request.getRequestURI(),
            "Tente novamente mais tarde ou contate o suporte."
        );
        return ResponseEntity.status(status).body(body);
    }

    // Você pode adicionar outros métodos @ExceptionHandler aqui para exceções específicas, 
    // como ClientAlreadyExistsException (400), ClientNotFoundException (404), etc.,
    // seguindo o mesmo padrão:
    // 1. Determinar o HttpStatus adequado,
    // 2. Logar a exceção (se desejado),
    // 3. Instanciar ErrorResponse preenchendo os campos,
    // 4. Retornar ResponseEntity com o corpo e status.
    
    // Exemplo para uma exceção customizada de recurso não encontrado:
    //
    // @ExceptionHandler(ClientNotFoundException.class)
    // public ResponseEntity<ErrorResponse> handleClientNotFoundException(ClientNotFoundException ex, HttpServletRequest request) {
    //     HttpStatus status = HttpStatus.NOT_FOUND;
    //     ErrorResponse body = new ErrorResponse(
    //         LocalDateTime.now(),
    //         status.value(),
    //         status.getReasonPhrase(),
    //         ex.getMessage(),
    //         request.getRequestURI(),
    //         "Verifique se o ID informado está correto."  // sugestão opcional
    //     );
    //     return ResponseEntity.status(status).body(body);
    // }
}
