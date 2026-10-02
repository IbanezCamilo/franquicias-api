package com.franquicias.api.domain.exception;

/**
 * Invariante del dominio incumplida: nombre con formato invalido, stock negativo, etc.
 * Se traduce a HTTP 400.
 */
public class ReglaDeNegocioException extends DominioException {

    public ReglaDeNegocioException(String mensaje) {
        super(mensaje);
    }
}
