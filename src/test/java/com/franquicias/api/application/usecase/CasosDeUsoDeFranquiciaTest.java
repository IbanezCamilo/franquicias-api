package com.franquicias.api.application.usecase;

import com.franquicias.api.domain.exception.NombreDuplicadoException;
import com.franquicias.api.domain.exception.RecursoNoEncontradoException;
import com.franquicias.api.domain.exception.ReglaDeNegocioException;
import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.model.Sucursal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Casos de uso de franquicia")
class CasosDeUsoDeFranquiciaTest {

    private RepositorioEnMemoria repositorio;

    @BeforeEach
    void prepararRepositorio() {
        repositorio = new RepositorioEnMemoria();
    }

    @Nested
    @DisplayName("CrearFranquicia")
    class Crear {

        @Test
        @DisplayName("persiste la franquicia con el nombre normalizado")
        void persisteLaFranquicia() {
            CrearFranquicia caso = new CrearFranquicia(repositorio);

            StepVerifier.create(caso.ejecutar("  Cafes del Valle  "))
                    .assertNext(franquicia -> {
                        assertThat(franquicia.id()).isNotNull();
                        assertThat(franquicia.nombre()).isEqualTo("Cafes del Valle");
                        assertThat(franquicia.sucursales()).isEmpty();
                    })
                    .verifyComplete();

            assertThat(repositorio.cantidadGuardada()).isEqualTo(1);
        }

        @Test
        @DisplayName("falla si ya existe una franquicia con ese nombre")
        void fallaSiElNombreEstaTomado() {
            repositorio.precargar(Franquicia.crear("Cafes del Valle"));
            CrearFranquicia caso = new CrearFranquicia(repositorio);

            StepVerifier.create(caso.ejecutar(" cafes del valle "))
                    .expectError(NombreDuplicadoException.class)
                    .verify();

            assertThat(repositorio.cantidadGuardada()).as("no se guarda nada").isEqualTo(1);
        }

        @Test
        @DisplayName("emite el error de validacion como senal, no como excepcion lanzada")
        void emiteErrorDeValidacionComoSenal() {
            CrearFranquicia caso = new CrearFranquicia(repositorio);

            // La llamada no debe lanzar: el error llega al suscribirse.
            var publicador = caso.ejecutar("  ");

            StepVerifier.create(publicador)
                    .expectError(ReglaDeNegocioException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("RenombrarFranquicia")
    class Renombrar {

        @Test
        @DisplayName("aplica el nuevo nombre conservando identidad y sucursales")
        void aplicaElNuevoNombre() {
            Franquicia original = repositorio.precargar(
                    Franquicia.crear("Cafes del Valle")
                            .agregarSucursal(Sucursal.crear("Sucursal Norte")));
            RenombrarFranquicia caso = new RenombrarFranquicia(repositorio);

            StepVerifier.create(caso.ejecutar(original.id(), "Cafes del Rio"))
                    .assertNext(franquicia -> {
                        assertThat(franquicia.id()).isEqualTo(original.id());
                        assertThat(franquicia.nombre()).isEqualTo("Cafes del Rio");
                        assertThat(franquicia.sucursales()).hasSize(1);
                    })
                    .verifyComplete();

            assertThat(repositorio.estadoDe(original.id()).nombre()).isEqualTo("Cafes del Rio");
        }

        @Test
        @DisplayName("permite renombrar una franquicia a su propio nombre")
        void permiteSuPropioNombre() {
            Franquicia original = repositorio.precargar(Franquicia.crear("Cafes del Valle"));
            RenombrarFranquicia caso = new RenombrarFranquicia(repositorio);

            StepVerifier.create(caso.ejecutar(original.id(), "CAFES DEL VALLE"))
                    .assertNext(franquicia ->
                            assertThat(franquicia.nombre()).isEqualTo("CAFES DEL VALLE"))
                    .verifyComplete();
        }

        @Test
        @DisplayName("falla si el nombre pertenece a otra franquicia")
        void fallaSiElNombreEsDeOtra() {
            Franquicia una = repositorio.precargar(Franquicia.crear("Cafes del Valle"));
            repositorio.precargar(Franquicia.crear("Cafes del Rio"));
            RenombrarFranquicia caso = new RenombrarFranquicia(repositorio);

            StepVerifier.create(caso.ejecutar(una.id(), "Cafes del Rio"))
                    .expectError(NombreDuplicadoException.class)
                    .verify();

            assertThat(repositorio.estadoDe(una.id()).nombre()).isEqualTo("Cafes del Valle");
        }

        @Test
        @DisplayName("falla si la franquicia no existe")
        void fallaSiNoExiste() {
            RenombrarFranquicia caso = new RenombrarFranquicia(repositorio);

            StepVerifier.create(caso.ejecutar(UUID.randomUUID(), "Cafes del Rio"))
                    .expectError(RecursoNoEncontradoException.class)
                    .verify();
        }
    }
}
