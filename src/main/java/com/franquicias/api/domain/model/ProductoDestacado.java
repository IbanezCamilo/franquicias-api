package com.franquicias.api.domain.model;

import java.util.UUID;

/**
 * Fila del reporte de mayor stock: el producto destacado junto con la sucursal a la
 * que pertenece, que es lo que exige el criterio de aceptacion.
 *
 * @param sucursalId     identificador de la sucursal propietaria
 * @param sucursalNombre nombre de la sucursal, para no obligar al cliente a resolverlo
 * @param producto       producto con mayor stock en esa sucursal
 */
public record ProductoDestacado(UUID sucursalId, String sucursalNombre, Producto producto) {
}
