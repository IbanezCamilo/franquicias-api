package com.franquicias.api.application.usecase;

import java.util.UUID;

import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.port.out.FranquiciaRepositoryPort;

import reactor.core.publisher.Mono;

public class ConsultarFranquicia {
    private final FranquiciaRepositoryPort repositorio;

    public ConsultarFranquicia(FranquiciaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Mono<Franquicia> ejecutar(UUID franquiciaId) {
        return repositorio.exigirPorId(franquiciaId);
    }
}
