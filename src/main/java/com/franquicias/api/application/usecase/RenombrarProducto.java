package com.franquicias.api.application.usecase;

import com.franquicias.api.domain.model.Producto;
import com.franquicias.api.domain.port.out.FranquiciaRepositoryPort;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Cambia el nombre de un producto, preservando su identidad y su stock. */
public class RenombrarProducto {

    private final FranquiciaRepositoryPort repositorio;

    public RenombrarProducto(FranquiciaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Mono<Producto> ejecutar(UUID franquiciaId, UUID sucursalId, UUID productoId, String nuevoNombre) {
        return repositorio.exigirPorId(franquiciaId)
                .map(franquicia -> franquicia.renombrarProducto(sucursalId, productoId, nuevoNombre))
                .flatMap(repositorio::guardar)
                .map(guardada -> guardada.exigirSucursal(sucursalId).exigirProducto(productoId));
    }
}
