package com.franquicias.api.application.usecase;

import com.franquicias.api.domain.exception.NombreDuplicadoException;
import com.franquicias.api.domain.exception.RecursoNoEncontradoException;
import com.franquicias.api.domain.exception.ReglaDeNegocioException;
import com.franquicias.api.domain.model.Franquicia;
import com.franquicias.api.domain.model.Producto;
import com.franquicias.api.domain.model.Sucursal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Casos de uso de producto")
class CasosDeUsoDeProductoTest {

    private RepositorioEnMemoria repositorio;
    private Sucursal norte;
    private Franquicia franquicia;

    @BeforeEach
    void prepararEscenario() {
        repositorio = new RepositorioEnMemoria();
        norte = Sucursal.crear("Sucursal Norte");
        franquicia = repositorio.precargar(
                Franquicia.crear("Cafes del Valle").agregarSucursal(norte));
    }

    private Producto precargarProducto(String nombre, int stock) {
        Producto producto = Producto.crear(nombre, stock);
        repositorio.precargar(repositorio.estadoDe(franquicia.id())
                .agregarProducto(norte.id(), producto));
        return producto;
    }

    @Nested
    @DisplayName("AgregarProducto")
    class Agregar {

        @Test
        @DisplayName("incorpora el producto al catalogo de la sucursal")
        void incorporaElProducto() {
            AgregarProducto caso = new AgregarProducto(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id(), norte.id(), "  Cafe Molido ", 25))
                    .assertNext(producto -> {
                        assertThat(producto.id()).isNotNull();
                        assertThat(producto.nombre()).isEqualTo("Cafe Molido");
                        assertThat(producto.stock()).isEqualTo(25);
                    })
                    .verifyComplete();

            assertThat(repositorio.estadoDe(franquicia.id()).exigirSucursal(norte.id()).productos())
                    .hasSize(1);
        }

        @Test
        @DisplayName("rechaza un stock negativo antes de tocar el repositorio")
        void rechazaStockNegativo() {
            AgregarProducto caso = new AgregarProducto(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id(), norte.id(), "Cafe Molido", -1))
                    .expectError(ReglaDeNegocioException.class)
                    .verify();

            assertThat(repositorio.estadoDe(franquicia.id()).exigirSucursal(norte.id()).productos())
                    .isEmpty();
        }

        @Test
        @DisplayName("falla si la sucursal no existe")
        void fallaSiLaSucursalNoExiste() {
            AgregarProducto caso = new AgregarProducto(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id(), UUID.randomUUID(), "Cafe", 1))
                    .expectError(RecursoNoEncontradoException.class)
                    .verify();
        }

        @Test
        @DisplayName("falla si la sucursal ya tiene un producto con ese nombre")
        void fallaSiElNombreEstaTomado() {
            precargarProducto("Cafe Molido", 10);
            AgregarProducto caso = new AgregarProducto(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id(), norte.id(), "cafe molido", 5))
                    .expectError(NombreDuplicadoException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("EliminarProducto")
    class Eliminar {

        @Test
        @DisplayName("retira el producto y completa sin emitir valor")
        void retiraElProducto() {
            Producto cafe = precargarProducto("Cafe Molido", 10);
            EliminarProducto caso = new EliminarProducto(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id(), norte.id(), cafe.id()))
                    .verifyComplete();

            assertThat(repositorio.estadoDe(franquicia.id()).exigirSucursal(norte.id()).productos())
                    .isEmpty();
        }

        @Test
        @DisplayName("falla si el producto no existe")
        void fallaSiNoExiste() {
            EliminarProducto caso = new EliminarProducto(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id(), norte.id(), UUID.randomUUID()))
                    .expectError(RecursoNoEncontradoException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("ActualizarStock")
    class Stock {

        @Test
        @DisplayName("aplica el nuevo stock y devuelve el producto actualizado")
        void aplicaElNuevoStock() {
            Producto cafe = precargarProducto("Cafe Molido", 10);
            ActualizarStock caso = new ActualizarStock(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id(), norte.id(), cafe.id(), 99))
                    .assertNext(producto -> {
                        assertThat(producto.id()).isEqualTo(cafe.id());
                        assertThat(producto.stock()).isEqualTo(99);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("acepta poner el stock a cero")
        void aceptaCero() {
            Producto cafe = precargarProducto("Cafe Molido", 10);
            ActualizarStock caso = new ActualizarStock(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id(), norte.id(), cafe.id(), 0))
                    .assertNext(producto -> assertThat(producto.stock()).isZero())
                    .verifyComplete();
        }

        @Test
        @DisplayName("rechaza un stock negativo y no altera lo persistido")
        void rechazaNegativo() {
            Producto cafe = precargarProducto("Cafe Molido", 10);
            ActualizarStock caso = new ActualizarStock(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id(), norte.id(), cafe.id(), -5))
                    .expectError(ReglaDeNegocioException.class)
                    .verify();

            assertThat(repositorio.estadoDe(franquicia.id())
                    .exigirSucursal(norte.id())
                    .exigirProducto(cafe.id())
                    .stock()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("RenombrarProducto")
    class Renombrar {

        @Test
        @DisplayName("aplica el nuevo nombre conservando identidad y stock")
        void aplicaElNuevoNombre() {
            Producto cafe = precargarProducto("Cafe Molido", 10);
            RenombrarProducto caso = new RenombrarProducto(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id(), norte.id(), cafe.id(), "Cafe en Grano"))
                    .assertNext(producto -> {
                        assertThat(producto.id()).isEqualTo(cafe.id());
                        assertThat(producto.nombre()).isEqualTo("Cafe en Grano");
                        assertThat(producto.stock()).isEqualTo(10);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("falla si el nombre pertenece a otro producto de la sucursal")
        void fallaSiElNombreEsDeOtro() {
            precargarProducto("Cafe Molido", 10);
            Producto te = precargarProducto("Te Verde", 5);
            RenombrarProducto caso = new RenombrarProducto(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id(), norte.id(), te.id(), "Cafe Molido"))
                    .expectError(NombreDuplicadoException.class)
                    .verify();
        }
    }

    @Nested
    @DisplayName("ConsultarProductosDestacados")
    class Destacados {

        @Test
        @DisplayName("devuelve un producto por sucursal con catalogo, omitiendo las vacias")
        void unoPorSucursalConCatalogo() {
            Sucursal sur = Sucursal.crear("Sucursal Sur");
            Sucursal vacia = Sucursal.crear("Sucursal Vacia");
            Producto cafe = Producto.crear("Cafe", 10);
            Producto te = Producto.crear("Te", 80);
            Producto azucar = Producto.crear("Azucar", 30);
            repositorio.precargar(repositorio.estadoDe(franquicia.id())
                    .agregarSucursal(sur)
                    .agregarSucursal(vacia)
                    .agregarProducto(norte.id(), cafe)
                    .agregarProducto(norte.id(), te)
                    .agregarProducto(sur.id(), azucar));
            ConsultarProductosDestacados caso = new ConsultarProductosDestacados(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id()))
                    .assertNext(destacado -> {
                        assertThat(destacado.sucursalNombre()).isEqualTo("Sucursal Norte");
                        assertThat(destacado.producto().nombre()).isEqualTo("Te");
                    })
                    .assertNext(destacado -> {
                        assertThat(destacado.sucursalNombre()).isEqualTo("Sucursal Sur");
                        assertThat(destacado.producto().nombre()).isEqualTo("Azucar");
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("completa vacio si ninguna sucursal tiene productos")
        void vacioSinProductos() {
            ConsultarProductosDestacados caso = new ConsultarProductosDestacados(repositorio);

            StepVerifier.create(caso.ejecutar(franquicia.id())).verifyComplete();
        }

        @Test
        @DisplayName("falla si la franquicia no existe")
        void fallaSiLaFranquiciaNoExiste() {
            ConsultarProductosDestacados caso = new ConsultarProductosDestacados(repositorio);

            StepVerifier.create(caso.ejecutar(UUID.randomUUID()))
                    .expectError(RecursoNoEncontradoException.class)
                    .verify();
        }
    }
}
