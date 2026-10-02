package com.franquicias.api.application.usecase;

import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.model.ProductoDestacado;
import com.franquicias.api.domain.port.out.FranquiciaRepositoryPort;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Criterio de aceptacion 7: el producto con mayor stock de cada sucursal de una
 * franquicia, indicando a que sucursal pertenece.
 *
 * <p>El calculo vive en el dominio; aqui solo se recupera el agregado y se expone
 * el resultado como un flujo.
 */
public class ConsultarProductosDestacados {

    private final FranquiciaRepositoryPort repositorio;

    public ConsultarProductosDestacados(FranquiciaRepositoryPort repositorio) {
        this.repositorio = repositorio;
    }

    public Flux<ProductoDestacado> ejecutar(UUID franquiciaId) {
        return repositorio.exigirPorId(franquiciaId)
                .flatMapIterable(Franquicia::productosConMayorStockPorSucursal);
    }
}
