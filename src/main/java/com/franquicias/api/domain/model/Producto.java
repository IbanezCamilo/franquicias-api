package com.franquicias.api.domain.model;

import com.franquicias.api.domain.exception.ReglaDeNegocioException;
import com.franquicias.api.domain.validation.Nombres;

import java.util.Objects;
import java.util.UUID;

/**
 * Producto ofertado en una sucursal. Inmutable: toda modificacion devuelve una
 * instancia nueva.
 *
 * @param id     identificador estable, independiente del nombre (que es modificable)
 * @param nombre nombre normalizado, unico dentro de su sucursal
 * @param stock  unidades disponibles, nunca negativas
 */
public record Producto(UUID id, String nombre, int stock) {

    public Producto {
        Objects.requireNonNull(id, "El identificador del producto es obligatorio");
        nombre = Nombres.normalizarYValidar("nombre del producto", nombre);
        validarStock(stock);
    }

    /** Crea un producto nuevo con identidad recien generada. */
    public static Producto crear(String nombre, int stock) {
        return new Producto(UUID.randomUUID(), nombre, stock);
    }

    public Producto conStock(int nuevoStock) {
        return new Producto(id, nombre, nuevoStock);
    }

    public Producto renombrado(String nuevoNombre) {
        return new Producto(id, nuevoNombre, stock);
    }

    private static void validarStock(int stock) {
        if (stock < 0) {
            throw new ReglaDeNegocioException("El stock no puede ser negativo");
        }
    }
}
