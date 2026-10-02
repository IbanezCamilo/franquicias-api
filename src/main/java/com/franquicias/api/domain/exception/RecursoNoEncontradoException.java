package com.franquicias.api.domain.exception;

import java.util.UUID;

/**
 * Se solicito una franquicia, sucursal o producto que no existe. Se traduce a HTTP 404.
 */
public class RecursoNoEncontradoException extends DominioException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }

    public static RecursoNoEncontradoException franquicia(UUID id) {
        return new RecursoNoEncontradoException("No existe una franquicia con el identificador " + id);
    }

    public static RecursoNoEncontradoException sucursal(UUID id) {
        return new RecursoNoEncontradoException("No existe una sucursal con el identificador " + id);
    }

    public static RecursoNoEncontradoException producto(UUID id) {
        return new RecursoNoEncontradoException("No existe un producto con el identificador " + id);
    }
}
