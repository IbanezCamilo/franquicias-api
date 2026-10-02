package com.franquicias.api.domain.exception;

/**
 * Raiz de las excepciones del dominio. El adaptador web las traduce a respuestas
 * {@code application/problem+json}; el dominio no conoce codigos HTTP.
 */
public abstract class DominioException extends RuntimeException {

    protected DominioException(String mensaje) {
        super(mensaje);
    }
}
