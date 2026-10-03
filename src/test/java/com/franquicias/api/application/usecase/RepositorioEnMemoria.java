package com.franquicias.api.application.usecase;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.port.out.FranquiciaRepositoryPort;
import com.franquicias.api.domain.validation.Nombres;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementacion en memoria del puerto, para probar los casos de uso sin Mongo ni
 * contexto de Spring. Replica la semantica de unicidad del adaptador real: la
 * comparacion de nombres ignora mayusculas y espacios.
 */
class RepositorioEnMemoria implements FranquiciaRepositoryPort {

    private final Map<UUID, Franquicia> almacen = new LinkedHashMap<>();
    
    /** Inserta directamente, sin pasar por los casos de uso, para preparar escenarios. */
    Franquicia precargar(Franquicia franquicia) {
        almacen.put(franquicia.id(), franquicia);
        return franquicia;
    }

    @Override
    public Flux<Franquicia> listarTodas() {
        // Imita el orden del adaptador real, que ordena por nombreNormalizado en Mongo.
        return Flux.fromIterable(almacen.values())
                .sort(Comparator.comparing(f -> Nombres.claveDeComparacion(f.nombre())));
    }

    Franquicia estadoDe(UUID id) {
        return almacen.get(id);
    }

    int cantidadGuardada() {
        return almacen.size();
    }

    @Override
    public Mono<Franquicia> buscarPorId(UUID id) {
        return Mono.justOrEmpty(almacen.get(id));
    }

    @Override
    public Mono<Boolean> existeConNombre(String nombre) {
        return Mono.just(almacen.values().stream()
                .anyMatch(franquicia -> Nombres.sonEquivalentes(franquicia.nombre(), nombre)));
    }

    @Override
    public Mono<Boolean> existeOtraConNombre(UUID idExcluido, String nombre) {
        return Mono.just(almacen.values().stream()
                .filter(franquicia -> !franquicia.id().equals(idExcluido))
                .anyMatch(franquicia -> Nombres.sonEquivalentes(franquicia.nombre(), nombre)));
    }

    @Override
    public Mono<Franquicia> guardar(Franquicia franquicia) {
        almacen.put(franquicia.id(), franquicia);
        return Mono.just(franquicia);
    }
}
