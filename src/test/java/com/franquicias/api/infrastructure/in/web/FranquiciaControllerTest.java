package com.franquicias.api.infrastructure.in.web;

import com.franquicias.api.application.usecase.ConsultarProductosDestacados;
import com.franquicias.api.application.usecase.CrearFranquicia;
import com.franquicias.api.application.usecase.RenombrarFranquicia;
import com.franquicias.api.domain.exception.NombreDuplicadoException;
import com.franquicias.api.domain.exception.RecursoNoEncontradoException;
import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.model.Producto;
import com.franquicias.api.domain.model.ProductoDestacado;
import com.franquicias.api.domain.model.Sucursal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = FranquiciaController.class)
@DisplayName("FranquiciaController")
class FranquiciaControllerTest {

    private static final String RUTA = "/api/v1/franquicias";

    @Autowired
    private WebTestClient cliente;

    @MockitoBean
    private CrearFranquicia crearFranquicia;

    @MockitoBean
    private RenombrarFranquicia renombrarFranquicia;

    @MockitoBean
    private ConsultarProductosDestacados consultarProductosDestacados;

    @Test
    @DisplayName("POST devuelve 201 con la franquicia creada")
    void creaLaFranquicia() {
        Franquicia creada = Franquicia.crear("Cafes del Valle");
        when(crearFranquicia.ejecutar("Cafes del Valle")).thenReturn(Mono.just(creada));

        cliente.post().uri(RUTA)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Cafes del Valle\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(creada.id().toString())
                .jsonPath("$.nombre").isEqualTo("Cafes del Valle")
                .jsonPath("$.sucursales").isArray();
    }

    @Test
    @DisplayName("POST devuelve 400 con el detalle por campo si el nombre esta en blanco")
    void rechazaNombreEnBlanco() {
        cliente.post().uri(RUTA)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"  \"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Peticion invalida")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.errors.nombre").exists();
    }

    @Test
    @DisplayName("POST devuelve 409 si el nombre ya existe")
    void rechazaNombreDuplicado() {
        when(crearFranquicia.ejecutar(any()))
                .thenReturn(Mono.error(NombreDuplicadoException.franquicia("Cafes del Valle")));

        cliente.post().uri(RUTA)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Cafes del Valle\"}")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Nombre duplicado")
                .jsonPath("$.type").isEqualTo("/errores/nombre-duplicado");
    }

    @Test
    @DisplayName("POST devuelve 400 si el cuerpo no es JSON valido")
    void rechazaCuerpoIlegible() {
        cliente.post().uri(RUTA)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{esto no es json")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("PATCH nombre devuelve 200 con la franquicia renombrada")
    void renombraLaFranquicia() {
        Franquicia renombrada = Franquicia.crear("Cafes del Rio");
        when(renombrarFranquicia.ejecutar(any(), eq("Cafes del Rio")))
                .thenReturn(Mono.just(renombrada));

        cliente.patch().uri(RUTA + "/" + UUID.randomUUID() + "/nombre")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Cafes del Rio\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nombre").isEqualTo("Cafes del Rio");
    }

    @Test
    @DisplayName("PATCH nombre devuelve 404 si la franquicia no existe")
    void renombrarInexistenteDevuelve404() {
        UUID id = UUID.randomUUID();
        when(renombrarFranquicia.ejecutar(eq(id), any()))
                .thenReturn(Mono.error(RecursoNoEncontradoException.franquicia(id)));

        cliente.patch().uri(RUTA + "/" + id + "/nombre")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Cafes del Rio\"}")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.title").isEqualTo("Recurso no encontrado")
                .jsonPath("$.detail").value(containsString(id.toString()));
    }

    @Test
    @DisplayName("PATCH nombre devuelve 400 si el identificador no es un UUID")
    void identificadorNoUuidDevuelve400() {
        cliente.patch().uri(RUTA + "/no-es-un-uuid/nombre")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Cafes del Rio\"}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("GET productos-top-stock devuelve un producto por sucursal con su sucursal")
    void devuelveProductosDestacados() {
        UUID franquiciaId = UUID.randomUUID();
        Sucursal norte = Sucursal.crear("Sucursal Norte");
        Sucursal sur = Sucursal.crear("Sucursal Sur");
        when(consultarProductosDestacados.ejecutar(franquiciaId)).thenReturn(Flux.just(
                new ProductoDestacado(norte.id(), norte.nombre(), Producto.crear("Te", 80)),
                new ProductoDestacado(sur.id(), sur.nombre(), Producto.crear("Azucar", 30))));

        cliente.get().uri(RUTA + "/" + franquiciaId + "/sucursales/productos-top-stock")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].sucursalNombre").isEqualTo("Sucursal Norte")
                .jsonPath("$[0].producto.nombre").isEqualTo("Te")
                .jsonPath("$[0].producto.stock").isEqualTo(80)
                .jsonPath("$[1].sucursalNombre").isEqualTo("Sucursal Sur")
                .jsonPath("$[1].producto.nombre").isEqualTo("Azucar");
    }

    @Test
    @DisplayName("GET productos-top-stock devuelve una lista vacia si no hay nada que destacar")
    void devuelveListaVacia() {
        UUID franquiciaId = UUID.randomUUID();
        when(consultarProductosDestacados.ejecutar(franquiciaId)).thenReturn(Flux.empty());

        cliente.get().uri(RUTA + "/" + franquiciaId + "/sucursales/productos-top-stock")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    @DisplayName("GET productos-top-stock devuelve 404 si la franquicia no existe")
    void destacadosDeInexistenteDevuelve404() {
        UUID franquiciaId = UUID.randomUUID();
        when(consultarProductosDestacados.ejecutar(franquiciaId))
                .thenReturn(Flux.error(RecursoNoEncontradoException.franquicia(franquiciaId)));

        cliente.get().uri(RUTA + "/" + franquiciaId + "/sucursales/productos-top-stock")
                .exchange()
                .expectStatus().isNotFound();
    }
}
