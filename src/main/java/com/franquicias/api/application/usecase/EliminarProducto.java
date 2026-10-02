package com.franquicias.api.application.usecase;

import com.franquicias.api.domain.port.out.FranquiciaRepositoryPort;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Retira un producto del catalogo de una sucursal. */
public class EliminarProducto {

    private final FranquiciaRepositoryPort repositorio;

    public EliminarProducto(FranquiciaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Mono<Void> ejecutar(UUID franquiciaId, UUID sucursalId, UUID productoId) {
        return repositorio.exigirPorId(franquiciaId)
                .map(franquicia -> franquicia.eliminarProducto(sucursalId, productoId))
                .flatMap(repositorio::guardar)
                .then();
    }
}
