package com.bm_nttdata.debitcard_ms.exception;

/**
 * Excepción personalizada para manejar errores relacionados con los recursos no encontrados.
 * Esta excepción es utilizada para manejar casos donde se intenta acceder a un
 * recurso que no existe.
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Construye una nueva excepción de servicio con el mensaje de error especificado.
     *
     * @param message Mensaje que describe la razón del error
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
