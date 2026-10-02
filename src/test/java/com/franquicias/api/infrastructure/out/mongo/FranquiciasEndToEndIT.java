package com.franquicias.api.infrastructure.out.mongo;

import com.franquicias.api.TestcontainersConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Recorrido completo contra un MongoDB real levantado con Testcontainers.
 *
 * <p>Es el unico test que toca la base de datos. Verifica lo que los dobles no pueden:
 * que el mapeo entre dominio y documento va y vuelve sin perder nada, que el indice
 * unico existe de verdad y que el agregado embebido se persiste completo.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@Import(TestcontainersConfiguration.class)
@DisplayName("Recorrido end to end sobre MongoDB real")
class FranquiciasEndToEndIT {

    private static final String RUTA = "/api/v1/franquicias";

    @Autowired
    private WebTestClient cliente;

    @Autowired
    private FranquiciaMongoRepository repositorio;

    @BeforeEach
    void limpiarColeccion() {
        repositorio.deleteAll().block();
    }

    @Test
    @DisplayName("cubre el flujo completo de la prueba, del alta al reporte de mayor stock")
    void flujoCompleto() {
        String franquiciaId = crearFranquicia("Cafes del Valle");

        String norte = crearSucursal(franquiciaId, "Sucursal Norte");
        String sur = crearSucursal(franquiciaId, "Sucursal Sur");
        crearSucursal(franquiciaId, "Sucursal Sin Catalogo");

        crearProducto(franquiciaId, norte, "Cafe Molido", 10);
        String teNorte = crearProducto(franquiciaId, norte, "Te Verde", 80);
        crearProducto(franquiciaId, sur, "Azucar", 30);
        String panelaSur = crearProducto(franquiciaId, sur, "Panela", 5);

        // El reporte devuelve un producto por sucursal con catalogo y omite la vacia.
        cliente.get().uri(RUTA + "/" + franquiciaId + "/sucursales/productos-top-stock")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].sucursalNombre").isEqualTo("Sucursal Norte")
                .jsonPath("$[0].producto.nombre").isEqualTo("Te Verde")
                .jsonPath("$[0].producto.stock").isEqualTo(80)
                .jsonPath("$[1].sucursalNombre").isEqualTo("Sucursal Sur")
                .jsonPath("$[1].producto.nombre").isEqualTo("Azucar");

        // Subir el stock de la panela cambia el producto destacado de la sucursal sur.
        cliente.patch().uri(RUTA + "/" + franquiciaId + "/sucursales/" + sur + "/productos/" + panelaSur + "/stock")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"stock\":500}")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.stock").isEqualTo(500);

        cliente.get().uri(RUTA + "/" + franquiciaId + "/sucursales/productos-top-stock")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[1].producto.nombre").isEqualTo("Panela")
                .jsonPath("$[1].producto.stock").isEqualTo(500);

        // Los renombrados preservan la identidad y, por tanto, las rutas siguen sirviendo.
        cliente.patch().uri(RUTA + "/" + franquiciaId + "/nombre")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Cafes del Rio\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.nombre").isEqualTo("Cafes del Rio");

        cliente.patch().uri(RUTA + "/" + franquiciaId + "/sucursales/" + norte + "/nombre")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Sucursal Centro\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.nombre").isEqualTo("Sucursal Centro");

        cliente.patch().uri(RUTA + "/" + franquiciaId + "/sucursales/" + norte + "/productos/" + teNorte + "/nombre")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"Te Negro\"}")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.nombre").isEqualTo("Te Negro");

        // Al eliminar el producto destacado, el reporte asciende al siguiente.
        cliente.delete().uri(RUTA + "/" + franquiciaId + "/sucursales/" + norte + "/productos/" + teNorte)
                .exchange()
                .expectStatus().isNoContent();

        cliente.get().uri(RUTA + "/" + franquiciaId + "/sucursales/productos-top-stock")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].sucursalNombre").isEqualTo("Sucursal Centro")
                .jsonPath("$[0].producto.nombre").isEqualTo("Cafe Molido");

        // Todo el arbol vive en un unico documento.
        assertThat(repositorio.count().block()).isEqualTo(1L);
    }

    @Test
    @DisplayName("el indice unico de nombre se crea y rechaza una franquicia duplicada")
    void rechazaFranquiciaDuplicada() {
        crearFranquicia("Cafes del Valle");

        cliente.post().uri(RUTA)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"  cafes del valle \"}")
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.title").isEqualTo("Nombre duplicado");

        assertThat(repositorio.count().block()).isEqualTo(1L);
    }

    @Test
    @DisplayName("el agregado va y vuelve de Mongo sin perder informacion")
    void elMapeoEsSimetrico() {
        String franquiciaId = crearFranquicia("Cafes del Valle");
        String norte = crearSucursal(franquiciaId, "Sucursal Norte");
        String cafe = crearProducto(franquiciaId, norte, "Cafe Molido", 42);

        var documento = repositorio.findById(UUID.fromString(franquiciaId)).block();

        assertThat(documento).isNotNull();
        assertThat(documento.nombre()).isEqualTo("Cafes del Valle");
        assertThat(documento.nombreNormalizado()).isEqualTo("cafes del valle");
        assertThat(documento.sucursales()).singleElement()
                .satisfies(sucursal -> {
                    assertThat(sucursal.id()).hasToString(norte);
                    assertThat(sucursal.nombre()).isEqualTo("Sucursal Norte");
                    assertThat(sucursal.productos()).singleElement().satisfies(producto -> {
                        assertThat(producto.id()).hasToString(cafe);
                        assertThat(producto.nombre()).isEqualTo("Cafe Molido");
                        assertThat(producto.stock()).isEqualTo(42);
                    });
                });
    }

    @Test
    @DisplayName("una franquicia inexistente devuelve 404 contra la base real")
    void franquiciaInexistenteDevuelve404() {
        cliente.get().uri(RUTA + "/" + UUID.randomUUID() + "/sucursales/productos-top-stock")
                .exchange()
                .expectStatus().isNotFound();
    }

    private String crearFranquicia(String nombre) {
        return extraerId(cliente.post().uri(RUTA)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"" + nombre + "\"}")
                .exchange()
                .expectStatus().isCreated());
    }

    private String crearSucursal(String franquiciaId, String nombre) {
        return extraerId(cliente.post().uri(RUTA + "/" + franquiciaId + "/sucursales")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"" + nombre + "\"}")
                .exchange()
                .expectStatus().isCreated());
    }

    private String crearProducto(String franquiciaId, String sucursalId, String nombre, int stock) {
        return extraerId(cliente.post()
                .uri(RUTA + "/" + franquiciaId + "/sucursales/" + sucursalId + "/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"nombre\":\"" + nombre + "\",\"stock\":" + stock + "}")
                .exchange()
                .expectStatus().isCreated());
    }

    private static String extraerId(WebTestClient.ResponseSpec respuesta) {
        Map<?, ?> cuerpo = respuesta.expectBody(Map.class).returnResult().getResponseBody();
        assertThat(cuerpo).isNotNull();
        Object id = cuerpo.get("id");
        assertThat(id).as("la respuesta debe traer el identificador del recurso creado").isNotNull();
        return String.valueOf(id);
    }
}
