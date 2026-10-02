package com.franquicias.api.infrastructure.in.web;

import com.franquicias.api.application.usecase.AgregarSucursal;
import com.franquicias.api.application.usecase.RenombrarSucursal;
import com.franquicias.api.domain.exception.NombreDuplicadoException;
import com.franquicias.api.domain.exception.RecursoNoEncontradoException;
import com.franquicias.api.domain.model.Sucursal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = SucursalController.class)
@DisplayName("SucursalController")
class SucursalControllerTest {

    private static final UUID FRANQUICIA_ID = UUID.randomUUID();
    private static final String RUTA = "/api/v1/franquicias/" + FRANQUICIA_ID + "/sucursales";

    @Autowired
    private WebTestClient cliente;

    @MockitoBean
    private AgregarSucursal agregarSucursal;

    @MockitoBean
    private RenombrarSucursal renombrarSucursal;

    @Test
    @DisplayName("POST devuelve 201 con la sucursal creada")
    void agregaLaSucursal() {
        Sucursal creada = Sucursal.crear("Sucursal Norte");
        when(agregarSucursal.ejecutar(FRANQUICIA_ID, "Sucursal Norte")).thenReturn(Mono.just(creada));

        cliente.post().uri(RUTA)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Sucursal Norte\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(creada.id().toString())
                .jsonPath("$.nombre").isEqualTo("Sucursal Norte")
                .jsonPath("$.productos").isArray();
    }

    @Test
    @DisplayName("POST devuelve 404 si la franquicia no existe")
    void franquiciaInexistenteDevuelve404() {
        when(agregarSucursal.ejecutar(any(), any()))
                .thenReturn(Mono.error(RecursoNoEncontradoException.franquicia(FRANQUICIA_ID)));

        cliente.post().uri(RUTA)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Sucursal Norte\"}")
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("POST devuelve 409 si la franquicia ya tiene una sucursal con ese nombre")
    void nombreDuplicadoDevuelve409() {
        when(agregarSucursal.ejecutar(any(), any()))
                .thenReturn(Mono.error(NombreDuplicadoException.sucursal("Sucursal Norte")));

        cliente.post().uri(RUTA)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Sucursal Norte\"}")
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    @DisplayName("POST devuelve 400 si falta el nombre")
    void nombreAusenteDevuelve400() {
        cliente.post().uri(RUTA)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.nombre").exists();
    }

    @Test
    @DisplayName("PATCH nombre devuelve 200 con la sucursal renombrada")
    void renombraLaSucursal() {
        Sucursal renombrada = Sucursal.crear("Sucursal Centro");
        when(renombrarSucursal.ejecutar(eq(FRANQUICIA_ID), any(), eq("Sucursal Centro")))
                .thenReturn(Mono.just(renombrada));

        cliente.patch().uri(RUTA + "/" + UUID.randomUUID() + "/nombre")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Sucursal Centro\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nombre").isEqualTo("Sucursal Centro");
    }

    @Test
    @DisplayName("PATCH nombre devuelve 404 si la sucursal no existe")
    void renombrarInexistenteDevuelve404() {
        UUID sucursalId = UUID.randomUUID();
        when(renombrarSucursal.ejecutar(any(), eq(sucursalId), any()))
                .thenReturn(Mono.error(RecursoNoEncontradoException.sucursal(sucursalId)));

        cliente.patch().uri(RUTA + "/" + sucursalId + "/nombre")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Sucursal Centro\"}")
                .exchange()
                .expectStatus().isNotFound();
    }
}
