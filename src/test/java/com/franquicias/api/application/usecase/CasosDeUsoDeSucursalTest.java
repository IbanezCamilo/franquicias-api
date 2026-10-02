package com.franquicias.api.application.usecase;

import com.franquicias.api.domain.exception.NombreDuplicadoException;
import com.franquicias.api.domain.exception.RecursoNoEncontradoException;
import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.model.Sucursal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Casos de uso de sucursal")
class CasosDeUsoDeSucursalTest {

    private RepositorioEnMemoria repositorio;

    @BeforeEach
    void prepararRepositorio() {
        repositorio = new RepositorioEnMemoria();
    }

    @Nested
    @DisplayName("AgregarSucursal")
    class Agregar {

        @Test
        @DisplayName("incorpora la sucursal a la franquicia y la devuelve")
        void incorporaLaSucursal() {
            Franquicia franquicia = repositorio.precargar(Franquicia.crear("Cafes del Valle"));
            AgregarSucursal caso = new AgregarSucursal(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id(), "  Sucursal Norte  "))
                    .assertNext(sucursal -> {
                        assertThat(sucursal.id()).isNotNull();
                        assertThat(sucursal.nombre()).isEqualTo("Sucursal Norte");
                        assertThat(sucursal.productos()).isEmpty();
                    })
                    .verifyComplete();

            assertThat(repositorio.estadoDe(franquicia.id()).sucursales()).hasSize(1);
        }

        @Test
        @DisplayName("falla si la franquicia no existe")
        void fallaSiLaFranquiciaNoExiste() {
            AgregarSucursal caso = new AgregarSucursal(repositorio);

            StepVerifier.create(caso.ejecutar(UUID.randomUUID(), "Sucursal Norte"))
                    .expectError(RecursoNoEncontradoException.class)
                    .verify();
        }

        @Test
        @DisplayName("falla si la franquicia ya tiene una sucursal con ese nombre")
        void fallaSiElNombreEstaTomado() {
            Franquicia franquicia = repositorio.precargar(
                    Franquicia.crear("Cafes del Valle").agregarSucursal(Sucursal.crear("Sucursal Norte")));
            AgregarSucursal caso = new AgregarSucursal(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id(), "sucursal norte"))
                    .expectError(NombreDuplicadoException.class)
                    .verify();

            assertThat(repositorio.estadoDe(franquicia.id()).sucursales())
                    .as("el estado persistido no cambia")
                    .hasSize(1);
        }
    }

    @Nested
    @DisplayName("RenombrarSucursal")
    class Renombrar {

        @Test
        @DisplayName("aplica el nuevo nombre conservando identidad y productos")
        void aplicaElNuevoNombre() {
            Sucursal norte = Sucursal.crear("Sucursal Norte");
            Franquicia franquicia = repositorio.precargar(
                    Franquicia.crear("Cafes del Valle").agregarSucursal(norte));
            RenombrarSucursal caso = new RenombrarSucursal(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id(), norte.id(), "Sucursal Centro"))
                    .assertNext(sucursal -> {
                        assertThat(sucursal.id()).isEqualTo(norte.id());
                        assertThat(sucursal.nombre()).isEqualTo("Sucursal Centro");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("falla si la sucursal no existe")
        void fallaSiLaSucursalNoExiste() {
            Franquicia franquicia = repositorio.precargar(Franquicia.crear("Cafes del Valle"));
            RenombrarSucursal caso = new RenombrarSucursal(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id(), UUID.randomUUID(), "Sucursal Centro"))
                    .expectError(RecursoNoEncontradoException.class)
                    .verify();
        }

        @Test
        @DisplayName("falla si el nombre pertenece a otra sucursal de la franquicia")
        void fallaSiElNombreEsDeOtra() {
            Sucursal norte = Sucursal.crear("Sucursal Norte");
            Sucursal sur = Sucursal.crear("Sucursal Sur");
            Franquicia franquicia = repositorio.precargar(Franquicia.crear("Cafes del Valle")
                    .agregarSucursal(norte)
                    .agregarSucursal(sur));
            RenombrarSucursal caso = new RenombrarSucursal(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id(), sur.id(), "Sucursal Norte"))
                    .expectError(NombreDuplicadoException.class)
                    .verify();
        }
    }
}
