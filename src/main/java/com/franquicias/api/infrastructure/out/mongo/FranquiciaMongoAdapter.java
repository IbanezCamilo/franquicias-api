package com.franquicias.api.infrastructure.out.mongo;

import com.franquicias.api.domain.exception.NombreDuplicadoException;
import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.port.out.FranquiciaRepositoryPort;
import com.franquicias.api.domain.validation.Nombres;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Adaptador de salida que implementa el puerto del dominio sobre MongoDB.
 *
 * <p>Traduce entre el modelo de dominio y el documento, y convierte los fallos
 * tecnicos del motor en excepciones con significado de negocio.
 */
@Component
public class FranquiciaMongoAdapter implements FranquiciaRepositoryPort {

    private final FranquiciaMongoRepository repositorio;

    public FranquiciaMongoAdapter(FranquiciaMongoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public Mono<Franquicia> buscarPorId(UUID id) {
        return repositorio.findById(id).map(FranquiciaDocument::aDominio);
    }

    @Override
    public Mono<Boolean> existeConNombre(String nombre) {
        return repositorio.existsByNombreNormalizado(Nombres.claveDeComparacion(nombre));
    }

    @Override
    public Mono<Boolean> existeOtraConNombre(UUID idExcluido, String nombre) {
        return repositorio.existsByNombreNormalizadoAndIdNot(
                Nombres.claveDeComparacion(nombre), idExcluido);
    }

    @Override
    public Mono<Franquicia> guardar(Franquicia franquicia) {
        return repositorio.save(FranquiciaDocument.desde(franquicia))
                .map(FranquiciaDocument::aDominio)
                // El indice unico cierra la ventana entre comprobar el nombre y guardar:
                // si dos peticiones concurrentes crean la misma franquicia, una falla aqui
                // y el cliente recibe un 409 en lugar de un 500.
                .onErrorMap(DuplicateKeyException.class,
                        error -> NombreDuplicadoException.franquicia(franquicia.nombre()));
    }
}
