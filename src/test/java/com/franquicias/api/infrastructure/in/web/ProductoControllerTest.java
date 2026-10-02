package com.franquicias.api.infrastructure.in.web;

import com.franquicias.api.application.usecase.ActualizarStock;
import com.franquicias.api.application.usecase.AgregarProducto;
import com.franquicias.api.application.usecase.EliminarProducto;
import com.franquicias.api.application.usecase.RenombrarProducto;
import com.franquicias.api.domain.exception.NombreDuplicadoException;
import com.franquicias.api.domain.exception.RecursoNoEncontradoException;
import com.franquicias.api.domain.model.Producto;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = ProductoController.class)
@DisplayName("ProductoController")
class ProductoControllerTest {

    private static final UUID FRANQUICIA_ID = UUID.randomUUID();
    private static final UUID SUCURSAL_ID = UUID.randomUUID();
    private static final String RUTA = "/api/v1/franquicias/" + FRANQUICIA_ID
            + "/sucursales/" + SUCURSAL_ID + "/productos";

    @Autowired
    private WebTestClient cliente;

    @MockitoBean
    private AgregarProducto agregarProducto;

    @MockitoBean
    private EliminarProducto eliminarProducto;

    @MockitoBean
    private ActualizarStock actualizarStock;

    @MockitoBean
    private RenombrarProducto renombrarProducto;

    @Test
    @DisplayName("POST devuelve 201 con el producto creado")
    void agregaElProducto() {
        Producto creado = Producto.crear("Cafe Molido", 120);
        when(agregarProducto.ejecutar(FRANQUICIA_ID, SUCURSAL_ID, "Cafe Molido", 120))
                .thenReturn(Mono.just(creado));

        cliente.post().uri(RUTA)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Cafe Molido\",\"stock\":120}")
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo(creado.id().toString())
                .jsonPath("$.nombre").isEqualTo("Cafe Molido")
                .jsonPath("$.stock").isEqualTo(120);
    }

    @Test
    @DisplayName("POST devuelve 400 si el stock es negativo")
    void stockNegativoDevuelve400() {
        cliente.post().uri(RUTA)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Cafe Molido\",\"stock\":-1}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.stock").isEqualTo("El stock no puede ser negativo");
    }

    @Test
    @DisplayName("POST devuelve 400 si falta el stock")
    void stockAusenteDevuelve400() {
        cliente.post().uri(RUTA)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Cafe Molido\"}")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.stock").exists();
    }

    @Test
    @DisplayName("POST devuelve 409 si la sucursal ya tiene un producto con ese nombre")
    void nombreDuplicadoDevuelve409() {
        when(agregarProducto.ejecutar(any(), any(), any(), anyInt()))
                .thenReturn(Mono.error(NombreDuplicadoException.producto("Cafe Molido")));

        cliente.post().uri(RUTA)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Cafe Molido\",\"stock\":10}")
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    @DisplayName("DELETE devuelve 204 sin cuerpo")
    void eliminaElProducto() {
        UUID productoId = UUID.randomUUID();
        when(eliminarProducto.ejecutar(FRANQUICIA_ID, SUCURSAL_ID, productoId))
                .thenReturn(Mono.empty());

        cliente.delete().uri(RUTA + "/" + productoId)
                .exchange()
                .expectStatus().isNoContent()
                .expectBody().isEmpty();
    }

    @Test
    @DisplayName("DELETE devuelve 404 si el producto no existe")
    void eliminarInexistenteDevuelve404() {
        UUID productoId = UUID.randomUUID();
        when(eliminarProducto.ejecutar(any(), any(), eq(productoId)))
                .thenReturn(Mono.error(RecursoNoEncontradoException.producto(productoId)));

        cliente.delete().uri(RUTA + "/" + productoId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("PATCH stock devuelve 200 con el producto actualizado")
    void actualizaElStock() {
        UUID productoId = UUID.randomUUID();
        Producto actualizado = Producto.crear("Cafe Molido", 250);
        when(actualizarStock.ejecutar(FRANQUICIA_ID, SUCURSAL_ID, productoId, 250))
                .thenReturn(Mono.just(actualizado));

        cliente.patch().uri(RUTA + "/" + productoId + "/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":250}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.stock").isEqualTo(250);
    }

    @Test
    @DisplayName("PATCH stock acepta el valor cero")
    void aceptaStockCero() {
        UUID productoId = UUID.randomUUID();
        when(actualizarStock.ejecutar(any(), any(), any(), eq(0)))
                .thenReturn(Mono.just(Producto.crear("Cafe Molido", 0)));

        cliente.patch().uri(RUTA + "/" + productoId + "/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":0}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.stock").isEqualTo(0);
    }

    @Test
    @DisplayName("PATCH stock devuelve 400 si el valor es negativo")
    void stockNegativoEnPatchDevuelve400() {
        cliente.patch().uri(RUTA + "/" + UUID.randomUUID() + "/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":-5}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @DisplayName("PATCH nombre devuelve 200 con el producto renombrado")
    void renombraElProducto() {
        UUID productoId = UUID.randomUUID();
        when(renombrarProducto.ejecutar(eq(FRANQUICIA_ID), eq(SUCURSAL_ID), eq(productoId),
                eq("Cafe en Grano")))
                .thenReturn(Mono.just(Producto.crear("Cafe en Grano", 10)));

        cliente.patch().uri(RUTA + "/" + productoId + "/nombre")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Cafe en Grano\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.nombre").isEqualTo("Cafe en Grano");
    }
}
