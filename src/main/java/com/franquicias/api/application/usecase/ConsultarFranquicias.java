package com.franquicias.api.application.usecase;

import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.port.out.FranquiciaRepositoryPort;

import reactor.core.publisher.Flux;

public class ConsultarFranquicias {

    private final FranquiciaRepositoryPort repositorio;

    public ConsultarFranquicias(FranquiciaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Flux<Franquicia> ejecutar() {
        return repositorio.listarTodas();
    }
}
