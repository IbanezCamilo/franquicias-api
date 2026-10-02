package com.franquicias.api.application.usecase;

import com.franquicias.api.domain.exception.NombreDuplicadoException;
import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.port.out.FranquiciaRepositoryPort;
import com.franquicias.api.domain.validation.Nombres;
import reactor.core.publisher.Mono;

import java.util.UUID;

/** Cambia el nombre de una franquicia, preservando su identidad y su contenido. */
public class RenombrarFranquicia {

    private final FranquiciaRepositoryPort repositorio;

    public RenombrarFranquicia(FranquiciaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Mono<Franquicia> ejecutar(UUID franquiciaId, String nuevoNombre) {
        return Mono.fromCallable(
                        () -> Nombres.normalizarYValidar("nombre de la franquicia", nuevoNombre))
                .flatMap(nombre -> repositorio.exigirPorId(franquiciaId)
                        .flatMap(franquicia -> repositorio.existeOtraConNombre(franquiciaId, nombre)
                                .flatMap(existe -> Boolean.TRUE.equals(existe)
                                        ? Mono.error(NombreDuplicadoException.franquicia(nombre))
                                        : repositorio.guardar(franquicia.renombrada(nombre)))));
    }
}
