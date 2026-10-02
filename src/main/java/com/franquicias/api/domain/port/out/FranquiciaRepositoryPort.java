package com.franquicias.api.domain.port.out;

import com.franquicias.api.domain.exception.RecursoNoEncontradoException;
import com.franquicias.api.domain.model.Franquicia;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Puerto de salida hacia el almacen de franquicias. El dominio declara que necesita
 * persistir y recuperar el agregado; el adaptador de infraestructura decide como.
 *
 * <p>Los tipos de Reactor forman parte del contrato porque toda la aplicacion es
 * reactiva de punta a punta. Es la unica dependencia externa que el dominio admite:
 * no entra aqui nada de Spring ni de MongoDB.
 */
public interface FranquiciaRepositoryPort {

    /** Recupera el agregado completo, o vacio si no existe. */
    Mono<Franquicia> buscarPorId(UUID id);

    /** Indica si ya hay una franquicia con ese nombre, ignorando mayusculas y espacios. */
    Mono<Boolean> existeConNombre(String nombre);

    /**
     * Indica si hay otra franquicia, distinta de la indicada, con ese nombre. Permite
     * validar un renombrado sin que la propia franquicia se bloquee a si misma.
     */
    Mono<Boolean> existeOtraConNombre(UUID idExcluido, String nombre);

    /** Guarda el agregado completo y devuelve el estado persistido. */
    Mono<Franquicia> guardar(Franquicia franquicia);

    /**
     * Recupera el agregado o falla con {@link RecursoNoEncontradoException}. Pedir una
     * franquicia inexistente es un error del dominio, no un resultado vacio legitimo,
     * asi que la politica vive aqui y no repetida en cada caso de uso.
     */
    default Mono<Franquicia> exigirPorId(UUID id) {
        return buscarPorId(id)
                .switchIfEmpty(Mono.error(() -> RecursoNoEncontradoException.franquicia(id)));
    }
}
