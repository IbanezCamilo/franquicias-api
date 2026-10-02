package com.franquicias.api.application.usecase;

import com.franquicias.api.domain.model.Producto;
import com.franquicias.api.domain.port.out.FranquiciaRepositoryPort;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Modifica el stock de un producto concreto de una sucursal. */
public class ActualizarStock {

    private final FranquiciaRepositoryPort repositorio;

    public ActualizarStock(FranquiciaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Mono<Producto> ejecutar(UUID franquiciaId, UUID sucursalId, UUID productoId, int nuevoStock) {
        return repositorio.exigirPorId(franquiciaId)
                .map(franquicia -> franquicia.actualizarStock(sucursalId, productoId, nuevoStock))
                .flatMap(repositorio::guardar)
                .map(guardada -> guardada.exigirSucursal(sucursalId).exigirProducto(productoId));
    }
}
