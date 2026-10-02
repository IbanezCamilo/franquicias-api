package com.franquicias.api.config;

import com.franquicias.api.application.usecase.ActualizarStock;
import com.franquicias.api.application.usecase.AgregarProducto;
import com.franquicias.api.application.usecase.AgregarSucursal;
import com.franquicias.api.application.usecase.ConsultarProductosDestacados;
import com.franquicias.api.application.usecase.CrearFranquicia;
import com.franquicias.api.application.usecase.EliminarProducto;
import com.franquicias.api.application.usecase.RenombrarFranquicia;
import com.franquicias.api.application.usecase.RenombrarProducto;
import com.franquicias.api.application.usecase.RenombrarSucursal;
import com.franquicias.api.domain.port.out.FranquiciaRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cableado de los casos de uso como beans.
 *
 * <p>Se declaran aqui, y no con {@code @Service} sobre cada clase, para que las capas
 * de dominio y aplicacion no importen nada de Spring: son POJOs que reciben el puerto
 * por constructor y se pueden instanciar en un test con un doble en memoria.
 */
@Configuration(proxyBeanMethods = false)
public class CasosDeUsoConfiguration {

    @Bean
    CrearFranquicia crearFranquicia(FranquiciaRepositoryPort repositorio) {
        return new CrearFranquicia(repositorio);
    }

    @Bean
    RenombrarFranquicia renombrarFranquicia(FranquiciaRepositoryPort repositorio) {
        return new RenombrarFranquicia(repositorio);
    }

    @Bean
    AgregarSucursal agregarSucursal(FranquiciaRepositoryPort repositorio) {
        return new AgregarSucursal(repositorio);
    }

    @Bean
    RenombrarSucursal renombrarSucursal(FranquiciaRepositoryPort repositorio) {
        return new RenombrarSucursal(repositorio);
    }

    @Bean
    AgregarProducto agregarProducto(FranquiciaRepositoryPort repositorio) {
        return new AgregarProducto(repositorio);
    }

    @Bean
    EliminarProducto eliminarProducto(FranquiciaRepositoryPort repositorio) {
        return new EliminarProducto(repositorio);
    }

    @Bean
    ActualizarStock actualizarStock(FranquiciaRepositoryPort repositorio) {
        return new ActualizarStock(repositorio);
    }

    @Bean
    RenombrarProducto renombrarProducto(FranquiciaRepositoryPort repositorio) {
        return new RenombrarProducto(repositorio);
    }

    @Bean
    ConsultarProductosDestacados consultarProductosDestacados(FranquiciaRepositoryPort repositorio) {
        return new ConsultarProductosDestacados(repositorio);
    }
}
