package com.bm_nttdata.debitcard_ms.exception;

/**
 * Excepción que se lanza cuando no se encuentra una tarjeta de débito específica.
 * Esta excepción es utilizada para manejar casos donde se intenta acceder a una
 * tarjeta de débito que no existe.
 */
public class DebitCardNotFoundException extends RuntimeException {

    /**
     * Construye una nueva excepción de tarjeta de débito no encontrada con el mensaje especificado.
     *
     * @param message Mensaje con la razón por la que no se encontró la tarjeta de débito
     */
    public DebitCardNotFoundException(String message) {
        super(message);
    }
}
