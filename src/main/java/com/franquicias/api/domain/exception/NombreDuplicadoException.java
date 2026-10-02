package com.franquicias.api.domain.exception;

/**
 * Viola la unicidad de nombre: de franquicia (global), de sucursal (dentro de su
 * franquicia) o de producto (dentro de su sucursal). Se traduce a HTTP 409.
 */
public class NombreDuplicadoException extends DominioException {

    public NombreDuplicadoException(String mensaje) {
        super(mensaje);
    }

    public static NombreDuplicadoException franquicia(String nombre) {
        return new NombreDuplicadoException("Ya existe una franquicia con el nombre '" + nombre + "'");
    }

    public static NombreDuplicadoException sucursal(String nombre) {
        return new NombreDuplicadoException(
                "La franquicia ya tiene una sucursal con el nombre '" + nombre + "'");
    }

    public static NombreDuplicadoException producto(String nombre) {
        return new NombreDuplicadoException(
                "La sucursal ya tiene un producto con el nombre '" + nombre + "'");
    }
}
