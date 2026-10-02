package com.franquicias.api.application.usecase;

import com.franquicias.api.domain.model.Sucursal;
import com.franquicias.api.domain.port.out.FranquiciaRepositoryPort;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Cambia el nombre de una sucursal, preservando su identidad y sus productos. */
public class RenombrarSucursal {

    private final FranquiciaRepositoryPort repositorio;

    public RenombrarSucursal(FranquiciaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Mono<Sucursal> ejecutar(UUID franquiciaId, UUID sucursalId, String nuevoNombre) {
        return repositorio.exigirPorId(franquiciaId)
                .map(franquicia -> franquicia.renombrarSucursal(sucursalId, nuevoNombre))
                .flatMap(repositorio::guardar)
                .map(guardada -> guardada.exigirSucursal(sucursalId));
    }
}
