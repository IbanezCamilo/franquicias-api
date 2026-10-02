package com.franquicias.api.infrastructure.out.mongo;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Repositorio reactivo de Spring Data sobre la coleccion de franquicias. Es un
 * detalle de infraestructura: el resto de la aplicacion solo conoce el puerto.
 */
public interface FranquiciaMongoRepository extends ReactiveMongoRepository<FranquiciaDocument, UUID> {

    Mono<Boolean> existsByNombreNormalizado(String nombreNormalizado);

    Mono<Boolean> existsByNombreNormalizadoAndIdNot(String nombreNormalizado, UUID id);
}
