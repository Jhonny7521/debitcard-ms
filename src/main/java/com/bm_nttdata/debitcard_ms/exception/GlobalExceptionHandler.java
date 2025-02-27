package com.bm_nttdata.debitcard_ms.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador global de excepciones para la aplicación.
 * Proporciona un manejo centralizado de las excepciones específicas del dominio,
 * transformándolas en respuestas HTTP apropiadas.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja las excepciones de tipo DebitCardNotFoundException.
     *
     * @param ex La excepción de tarjeta de débito no encontrada
     * @return ResponseEntity con los detalles del error y estado HTTP 404
     */
    @ExceptionHandler(DebitCardNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDebitCardNotFoundException(
            DebitCardNotFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "DEBIT_CARD_NOT_FOUND");
    }

    /**
     * Maneja las excepciones de tipo AccountNotFoundException.
     *
     * @param ex La excepción de cuenta no encontrada
     * @return ResponseEntity con los detalles del error y estado HTTP 404
     */
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleAccountNotFoundException(
            AccountNotFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "ACCOUNT_NOT_FOUND");
    }

    /**
     * Maneja las excepciones de tipo BusinessRuleException.
     *
     * @param ex La excepción de regla de negocio
     * @return ResponseEntity con los detalles del error y estado HTTP 422
     */
    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRuleException(
            BusinessRuleException ex) {
        return createErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), "BUSINESS_RULE_VIOLATION");
    }

    /**
     * Maneja las excepciones de tipo ServiceException.
     *
     * @param ex La excepción de servicio
     * @return ResponseEntity con los detalles del error y estado HTTP 500
     */
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<Map<String, Object>> handleServiceException(ServiceException ex) {
        return createErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), "SERVICE_EXCEPTION");
    }

    /**
     * Maneja las excepciones de tipo ApiInvalidRequestException.
     *
     * @param ex La excepción de solicitud inválida
     * @return ResponseEntity con los detalles del error y estado HTTP 400
     */
    @ExceptionHandler(ApiInvalidRequestException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRequestException(
            ApiInvalidRequestException ex) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), "BAD_REQUEST");
    }

    /**
     * Maneja las excepciones de tipo ResourceNotFoundException.
     *
     * @param ex La excepción de recurso no encontrado
     * @return ResponseEntity con los detalles del error y estado HTTP 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex) {
        return createErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), "RESOURCE_NOT_FOUND");
    }

    /**
     * Crea una respuesta de error estandarizada.
     *
     * @param status Estado HTTP de la respuesta
     * @param message Mensaje de error
     * @param code Código de error específico
     * @return ResponseEntity con el cuerpo del error estructurado
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(
            HttpStatus status, String message, String code) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", message);
        body.put("code", code);
        return new ResponseEntity<>(body, status);
    }
}
