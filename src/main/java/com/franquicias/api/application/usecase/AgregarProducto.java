package com.franquicias.api.application.usecase;

import com.franquicias.api.domain.model.Producto;
import com.franquicias.api.domain.port.out.FranquiciaRepositoryPort;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Agrega un producto al catalogo de una sucursal. */
public class AgregarProducto {

    private final FranquiciaRepositoryPort repositorio;

    public AgregarProducto(FranquiciaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Mono<Producto> ejecutar(UUID franquiciaId, UUID sucursalId, String nombre, int stock) {
        return Mono.fromCallable(() -> Producto.crear(nombre, stock))
                .flatMap(producto -> repositorio.exigirPorId(franquiciaId)
                        .map(franquicia -> franquicia.agregarProducto(sucursalId, producto))
                        .flatMap(repositorio::guardar)
                        .map(guardada -> guardada.exigirSucursal(sucursalId)
                                .exigirProducto(producto.id())));
    }
}
