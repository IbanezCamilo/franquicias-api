package com.franquicias.api.application.usecase;

import com.franquicias.api.domain.exception.NombreDuplicadoException;
import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.port.out.FranquiciaRepositoryPort;
import reactor.core.publisher.Mono;

/** Da de alta una franquicia, cuyo nombre debe ser unico en todo el sistema. */
public class CrearFranquicia {

    private final FranquiciaRepositoryPort repositorio;

    public CrearFranquicia(FranquiciaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Mono<Franquicia> ejecutar(String nombre) {
        // fromCallable convierte la validacion del dominio en una senal de error
        // en lugar de una excepcion lanzada antes de la suscripcion.
        return Mono.fromCallable(() -> Franquicia.crear(nombre))
                .flatMap(franquicia -> repositorio.existeConNombre(franquicia.nombre())
                        .flatMap(existe -> Boolean.TRUE.equals(existe)
                                ? Mono.error(NombreDuplicadoException.franquicia(franquicia.nombre()))
                                : repositorio.guardar(franquicia)));
    }
}
