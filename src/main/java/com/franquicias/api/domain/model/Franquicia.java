package com.franquicias.api.domain.model;

import com.franquicias.api.domain.exception.NombreDuplicadoException;
import com.franquicias.api.domain.exception.RecursoNoEncontradoException;
import com.franquicias.api.domain.validation.Nombres;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * Raiz del agregado: una franquicia y el arbol completo de sucursales y productos
 * que la componen. Inmutable, toda modificacion devuelve una instancia nueva.
 *
 * <p>Las sucursales no tienen ciclo de vida propio fuera de su franquicia, por eso
 * forman parte del mismo agregado y se persisten en un unico documento.
 *
 * @param id          identificador estable, independiente del nombre
 * @param nombre      nombre normalizado, unico en todo el sistema
 * @param sucursales  sucursales de la franquicia, sin nombres repetidos
 */
public record Franquicia(UUID id, String nombre, List<Sucursal> sucursales) {

    public Franquicia {
        Objects.requireNonNull(id, "El identificador de la franquicia es obligatorio");
        nombre = Nombres.normalizarYValidar("nombre de la franquicia", nombre);
        sucursales = List.copyOf(Objects.requireNonNullElseGet(sucursales, List::of));
    }

    /** Crea una franquicia nueva, sin sucursales, con identidad recien generada. */
    public static Franquicia crear(String nombre) {
        return new Franquicia(UUID.randomUUID(), nombre, List.of());
    }

    public Franquicia renombrada(String nuevoNombre) {
        return new Franquicia(id, nuevoNombre, sucursales);
    }

    public Franquicia agregarSucursal(Sucursal sucursal) {
        if (contieneNombre(sucursal.nombre(), null)) {
            throw NombreDuplicadoException.sucursal(sucursal.nombre());
        }
        List<Sucursal> actualizadas = new ArrayList<>(sucursales);
        actualizadas.add(sucursal);
        return new Franquicia(id, nombre, actualizadas);
    }

    public Franquicia renombrarSucursal(UUID sucursalId, String nuevoNombre) {
        exigirSucursal(sucursalId);
        // Se excluye la propia sucursal: renombrarla a su mismo nombre no es un conflicto.
        if (contieneNombre(nuevoNombre, sucursalId)) {
            throw NombreDuplicadoException.sucursal(nuevoNombre.strip());
        }
        return reemplazarSucursal(sucursalId, sucursal -> sucursal.renombrada(nuevoNombre));
    }

    public Franquicia agregarProducto(UUID sucursalId, Producto producto) {
        return reemplazarSucursal(sucursalId, sucursal -> sucursal.agregarProducto(producto));
    }

    public Franquicia eliminarProducto(UUID sucursalId, UUID productoId) {
        return reemplazarSucursal(sucursalId, sucursal -> sucursal.eliminarProducto(productoId));
    }

    public Franquicia actualizarStock(UUID sucursalId, UUID productoId, int nuevoStock) {
        return reemplazarSucursal(sucursalId,
                sucursal -> sucursal.actualizarStock(productoId, nuevoStock));
    }

    public Franquicia renombrarProducto(UUID sucursalId, UUID productoId, String nuevoNombre) {
        return reemplazarSucursal(sucursalId,
                sucursal -> sucursal.renombrarProducto(productoId, nuevoNombre));
    }

    /**
     * Producto con mayor stock de cada sucursal. Las sucursales sin productos se
     * omiten del resultado: sin catalogo no hay producto que reportar.
     */
    public List<ProductoDestacado> productosConMayorStockPorSucursal() {
        return sucursales.stream()
                .flatMap(sucursal -> sucursal.productoConMayorStock()
                        .map(producto -> new ProductoDestacado(sucursal.id(), sucursal.nombre(), producto))
                        .stream())
                .toList();
    }

    public Sucursal exigirSucursal(UUID sucursalId) {
        return sucursales.stream()
                .filter(sucursal -> sucursal.id().equals(sucursalId))
                .findFirst()
                .orElseThrow(() -> RecursoNoEncontradoException.sucursal(sucursalId));
    }

    private Franquicia reemplazarSucursal(UUID sucursalId, UnaryOperator<Sucursal> cambio) {
        exigirSucursal(sucursalId);
        return new Franquicia(id, nombre, sucursales.stream()
                .map(sucursal -> sucursal.id().equals(sucursalId) ? cambio.apply(sucursal) : sucursal)
                .toList());
    }

    /** Indica si ya hay una sucursal con ese nombre, ignorando opcionalmente una por id. */
    private boolean contieneNombre(String candidato, UUID idExcluido) {
        return sucursales.stream()
                .filter(sucursal -> !sucursal.id().equals(idExcluido))
                .anyMatch(sucursal -> Nombres.sonEquivalentes(sucursal.nombre(), candidato));
    }
}
