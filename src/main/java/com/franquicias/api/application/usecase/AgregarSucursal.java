package com.franquicias.api.application.usecase;

import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.model.Sucursal;
import com.franquicias.api.domain.port.out.FranquiciaRepositoryPort;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Agrega una sucursal a una franquicia existente. */
public class AgregarSucursal {

    private final FranquiciaRepositoryPort repositorio;

    public AgregarSucursal(FranquiciaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Mono<Sucursal> ejecutar(UUID franquiciaId, String nombreSucursal) {
        return Mono.fromCallable(() -> Sucursal.crear(nombreSucursal))
                .flatMap(sucursal -> repositorio.exigirPorId(franquiciaId)
                        .map(franquicia -> franquicia.agregarSucursal(sucursal))
                        .flatMap(repositorio::guardar)
                        // Se devuelve la sucursal tal como quedo persistida.
                        .map(guardada -> guardada.exigirSucursal(sucursal.id())));
    }
}
