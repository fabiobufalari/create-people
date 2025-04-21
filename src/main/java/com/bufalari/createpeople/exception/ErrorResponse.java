package com.bufalari.createpeople.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;

/**
 * Classe de modelo para respostas de erro da API.
 * Contém informações sobre o erro ocorrido, para serem retornadas ao cliente.
 */
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String suggestion;

    // Construtor padrão (necessário para desserialização e uso flexível)
    public ErrorResponse() {
        // inicialização default
    }

    // Construtor sem sugestão (define suggestion como null)
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path) {
        this(timestamp, status, error, message, path, null);
    }

    // Construtor completo
    public ErrorResponse(LocalDateTime timestamp, int status, String error, String message, String path, String suggestion) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.suggestion = suggestion;
    }

    // Getters (para serialização JSON)
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    public int getStatus() {
        return status;
    }
    public String getError() {
        return error;
    }
    public String getMessage() {
        return message;
    }
    public String getPath() {
        return path;
    }
    public String getSuggestion() {
        return suggestion;
    }

    // Opcional: você pode adicionar setters ou builders se necessário, 
    // mas geralmente não é preciso modificar o ErrorResponse após criado.
}
