package com.franquicias.api.domain.model;

import com.franquicias.api.domain.exception.NombreDuplicadoException;
import com.franquicias.api.domain.exception.RecursoNoEncontradoException;
import com.franquicias.api.domain.validation.Nombres;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * Sucursal de una franquicia, con su catalogo de productos. Inmutable: toda
 * modificacion devuelve una instancia nueva.
 *
 * @param id        identificador estable, independiente del nombre
 * @param nombre    nombre normalizado, unico dentro de su franquicia
 * @param productos catalogo, sin nombres repetidos
 */
public record Sucursal(UUID id, String nombre, List<Producto> productos) {

    /**
     * Desempate del reporte de mayor stock: primero el stock mas alto y, ante un
     * empate, el nombre en orden ascendente, para que la respuesta sea determinista.
     */
    private static final Comparator<Producto> MEJOR_STOCK_PRIMERO =
            Comparator.comparingInt(Producto::stock).reversed()
                    .thenComparing(Producto::nombre);

    public Sucursal {
        Objects.requireNonNull(id, "El identificador de la sucursal es obligatorio");
        nombre = Nombres.normalizarYValidar("nombre de la sucursal", nombre);
        productos = List.copyOf(Objects.requireNonNullElseGet(productos, List::of));
    }

    /** Crea una sucursal nueva, sin productos, con identidad recien generada. */
    public static Sucursal crear(String nombre) {
        return new Sucursal(UUID.randomUUID(), nombre, List.of());
    }

    public Sucursal renombrada(String nuevoNombre) {
        return new Sucursal(id, nuevoNombre, productos);
    }

    public Sucursal agregarProducto(Producto producto) {
        if (contieneNombre(producto.nombre(), null)) {
            throw NombreDuplicadoException.producto(producto.nombre());
        }
        List<Producto> actualizados = new ArrayList<>(productos);
        actualizados.add(producto);
        return new Sucursal(id, nombre, actualizados);
    }

    public Sucursal eliminarProducto(UUID productoId) {
        exigirProducto(productoId);
        return new Sucursal(id, nombre,
                productos.stream().filter(p -> !p.id().equals(productoId)).toList());
    }

    public Sucursal actualizarStock(UUID productoId, int nuevoStock) {
        return reemplazarProducto(productoId, producto -> producto.conStock(nuevoStock));
    }

    public Sucursal renombrarProducto(UUID productoId, String nuevoNombre) {
        exigirProducto(productoId);
        // Se excluye el propio producto: renombrarlo a su mismo nombre no es un conflicto.
        if (contieneNombre(nuevoNombre, productoId)) {
            throw NombreDuplicadoException.producto(nuevoNombre.strip());
        }
        return reemplazarProducto(productoId, producto -> producto.renombrado(nuevoNombre));
    }

    /**
     * Producto con mayor stock de esta sucursal, o vacio si no tiene productos.
     * Una sucursal sin catalogo no aporta ninguna fila al reporte.
     */
    public Optional<Producto> productoConMayorStock() {
        // min() sobre un comparador que ordena el mejor primero: devuelve el de mayor
        // stock y, ante empate, el primero alfabeticamente.
        return productos.stream().min(MEJOR_STOCK_PRIMERO);
    }

    public Producto exigirProducto(UUID productoId) {
        return productos.stream()
                .filter(producto -> producto.id().equals(productoId))
                .findFirst()
                .orElseThrow(() -> RecursoNoEncontradoException.producto(productoId));
    }

    private Sucursal reemplazarProducto(UUID productoId, UnaryOperator<Producto> cambio) {
        exigirProducto(productoId);
        return new Sucursal(id, nombre, productos.stream()
                .map(producto -> producto.id().equals(productoId) ? cambio.apply(producto) : producto)
                .toList());
    }

    /** Indica si ya hay un producto con ese nombre, ignorando opcionalmente uno por id. */
    private boolean contieneNombre(String candidato, UUID idExcluido) {
        return productos.stream()
                .filter(producto -> !producto.id().equals(idExcluido))
                .anyMatch(producto -> Nombres.sonEquivalentes(producto.nombre(), candidato));
    }
}
