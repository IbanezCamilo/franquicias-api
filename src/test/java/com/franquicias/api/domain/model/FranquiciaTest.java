package com.franquicias.api.domain.model;

import com.franquicias.api.domain.exception.NombreDuplicadoException;
import com.franquicias.api.domain.exception.RecursoNoEncontradoException;
import com.franquicias.api.domain.exception.ReglaDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@DisplayName("Franquicia")
class FranquiciaTest {

    @Test
    @DisplayName("se crea sin sucursales y con el nombre normalizado")
    void seCreaVacia() {
        Franquicia franquicia = Franquicia.crear("  Cafes del Valle  ");

        assertThat(franquicia.id()).isNotNull();
        assertThat(franquicia.nombre()).isEqualTo("Cafes del Valle");
        assertThat(franquicia.sucursales()).isEmpty();
    }

    @Test
    @DisplayName("rechaza un nombre en blanco")
    void rechazaNombreEnBlanco() {
        assertThatThrownBy(() -> Franquicia.crear("  "))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("nombre de la franquicia");
    }

    @Test
    @DisplayName("renombrada conserva identidad y sucursales")
    void renombradaConservaElResto() {
        Franquicia original = Franquicia.crear("Cafes del Valle")
                .agregarSucursal(Sucursal.crear("Sucursal Norte"));

        Franquicia renombrada = original.renombrada("Cafes del Rio");

        assertThat(renombrada.id()).isEqualTo(original.id());
        assertThat(renombrada.nombre()).isEqualTo("Cafes del Rio");
        assertThat(renombrada.sucursales()).isEqualTo(original.sucursales());
    }

    @Nested
    @DisplayName("agregarSucursal")
    class AgregarSucursal {

        @Test
        @DisplayName("incorpora la sucursal sin mutar la franquicia original")
        void incorporaLaSucursal() {
            Franquicia original = Franquicia.crear("Cafes del Valle");

            Franquicia conSucursal = original.agregarSucursal(Sucursal.crear("Sucursal Norte"));

            assertThat(conSucursal.sucursales()).hasSize(1);
            assertThat(original.sucursales()).as("el original no se altera").isEmpty();
        }

        @Test
        @DisplayName("rechaza un nombre ya presente aunque difiera en mayusculas")
        void rechazaNombreDuplicado() {
            Franquicia franquicia = Franquicia.crear("Cafes del Valle")
                    .agregarSucursal(Sucursal.crear("Sucursal Norte"));
            Sucursal duplicada = Sucursal.crear(" sucursal norte ");

            assertThatThrownBy(() -> franquicia.agregarSucursal(duplicada))
                    .isInstanceOf(NombreDuplicadoException.class);
        }
    }

    @Nested
    @DisplayName("renombrarSucursal")
    class RenombrarSucursal {

        @Test
        @DisplayName("aplica el nuevo nombre conservando la identidad y los productos")
        void aplicaElNuevoNombre() {
            Sucursal norte = Sucursal.crear("Sucursal Norte");
            Franquicia franquicia = Franquicia.crear("Cafes del Valle")
                    .agregarSucursal(norte)
                    .agregarProducto(norte.id(), Producto.crear("Cafe", 10));

            Franquicia resultado = franquicia.renombrarSucursal(norte.id(), "Sucursal Centro");

            assertThat(resultado.exigirSucursal(norte.id()).nombre()).isEqualTo("Sucursal Centro");
            assertThat(resultado.exigirSucursal(norte.id()).productos()).hasSize(1);
        }

        @Test
        @DisplayName("rechaza el nombre de otra sucursal de la misma franquicia")
        void rechazaElNombreDeOtra() {
            Sucursal norte = Sucursal.crear("Sucursal Norte");
            Sucursal sur = Sucursal.crear("Sucursal Sur");
            Franquicia franquicia = Franquicia.crear("Cafes del Valle")
                    .agregarSucursal(norte)
                    .agregarSucursal(sur);

            assertThatThrownBy(() -> franquicia.renombrarSucursal(sur.id(), "Sucursal Norte"))
                    .isInstanceOf(NombreDuplicadoException.class);
        }

        @Test
        @DisplayName("falla si la sucursal no existe")
        void fallaSiNoExiste() {
            Franquicia franquicia = Franquicia.crear("Cafes del Valle");
            UUID inexistente = UUID.randomUUID();

            assertThatThrownBy(() -> franquicia.renombrarSucursal(inexistente, "Sucursal Centro"))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("operaciones sobre productos anidados")
    class OperacionesAnidadas {

        @Test
        @DisplayName("agrega un producto a la sucursal indicada")
        void agregaProducto() {
            Sucursal norte = Sucursal.crear("Sucursal Norte");
            Sucursal sur = Sucursal.crear("Sucursal Sur");
            Franquicia franquicia = Franquicia.crear("Cafes del Valle")
                    .agregarSucursal(norte)
                    .agregarSucursal(sur);

            Franquicia resultado = franquicia.agregarProducto(norte.id(), Producto.crear("Cafe", 10));

            assertThat(resultado.exigirSucursal(norte.id()).productos()).hasSize(1);
            assertThat(resultado.exigirSucursal(sur.id()).productos()).isEmpty();
        }

        @Test
        @DisplayName("propaga el fallo si la sucursal destino no existe")
        void fallaSiLaSucursalNoExiste() {
            Franquicia franquicia = Franquicia.crear("Cafes del Valle");
            UUID inexistente = UUID.randomUUID();
            Producto cafe = Producto.crear("Cafe", 10);

            assertThatThrownBy(() -> franquicia.agregarProducto(inexistente, cafe))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining(inexistente.toString());
        }

        @Test
        @DisplayName("elimina, actualiza el stock y renombra el producto de la sucursal correcta")
        void modificaElProductoCorrecto() {
            Sucursal norte = Sucursal.crear("Sucursal Norte");
            Producto cafe = Producto.crear("Cafe", 10);
            Producto te = Producto.crear("Te", 5);
            Franquicia franquicia = Franquicia.crear("Cafes del Valle")
                    .agregarSucursal(norte)
                    .agregarProducto(norte.id(), cafe)
                    .agregarProducto(norte.id(), te);

            Franquicia conStock = franquicia.actualizarStock(norte.id(), cafe.id(), 99);
            Franquicia renombrada = conStock.renombrarProducto(norte.id(), cafe.id(), "Cafe en Grano");
            Franquicia sinTe = renombrada.eliminarProducto(norte.id(), te.id());

            assertThat(sinTe.exigirSucursal(norte.id()).productos())
                    .extracting(Producto::nombre, Producto::stock)
                    .containsExactly(tuple("Cafe en Grano", 99));
        }

        @Test
        @DisplayName("el mismo nombre de producto puede repetirse en sucursales distintas")
        void mismoNombreEnSucursalesDistintas() {
            Sucursal norte = Sucursal.crear("Sucursal Norte");
            Sucursal sur = Sucursal.crear("Sucursal Sur");
            Franquicia franquicia = Franquicia.crear("Cafes del Valle")
                    .agregarSucursal(norte)
                    .agregarSucursal(sur)
                    .agregarProducto(norte.id(), Producto.crear("Cafe", 10));

            Franquicia resultado = franquicia.agregarProducto(sur.id(), Producto.crear("Cafe", 7));

            assertThat(resultado.exigirSucursal(sur.id()).productos()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("productosConMayorStockPorSucursal")
    class ReporteDeMayorStock {

        @Test
        @DisplayName("devuelve un producto por cada sucursal con catalogo")
        void unoPorSucursal() {
            Sucursal norte = Sucursal.crear("Sucursal Norte");
            Sucursal sur = Sucursal.crear("Sucursal Sur");
            Franquicia franquicia = Franquicia.crear("Cafes del Valle")
                    .agregarSucursal(norte)
                    .agregarSucursal(sur)
                    .agregarProducto(norte.id(), Producto.crear("Cafe", 10))
                    .agregarProducto(norte.id(), Producto.crear("Te", 80))
                    .agregarProducto(sur.id(), Producto.crear("Azucar", 30))
                    .agregarProducto(sur.id(), Producto.crear("Panela", 5));

            assertThat(franquicia.productosConMayorStockPorSucursal())
                    .extracting(
                            ProductoDestacado::sucursalNombre,
                            destacado -> destacado.producto().nombre(),
                            destacado -> destacado.producto().stock())
                    .containsExactly(
                            tuple("Sucursal Norte", "Te", 80),
                            tuple("Sucursal Sur", "Azucar", 30));
        }

        @Test
        @DisplayName("omite las sucursales sin productos")
        void omiteSucursalesVacias() {
            Sucursal norte = Sucursal.crear("Sucursal Norte");
            Sucursal vacia = Sucursal.crear("Sucursal Vacia");
            Franquicia franquicia = Franquicia.crear("Cafes del Valle")
                    .agregarSucursal(norte)
                    .agregarSucursal(vacia)
                    .agregarProducto(norte.id(), Producto.crear("Cafe", 10));

            assertThat(franquicia.productosConMayorStockPorSucursal())
                    .extracting(ProductoDestacado::sucursalNombre)
                    .containsExactly("Sucursal Norte");
        }

        @Test
        @DisplayName("esta vacio si la franquicia no tiene sucursales")
        void vacioSinSucursales() {
            assertThat(Franquicia.crear("Cafes del Valle").productosConMayorStockPorSucursal())
                    .isEmpty();
        }

        @Test
        @DisplayName("esta vacio si ninguna sucursal tiene productos")
        void vacioSinProductos() {
            Franquicia franquicia = Franquicia.crear("Cafes del Valle")
                    .agregarSucursal(Sucursal.crear("Sucursal Norte"))
                    .agregarSucursal(Sucursal.crear("Sucursal Sur"));

            assertThat(franquicia.productosConMayorStockPorSucursal()).isEmpty();
        }

        @Test
        @DisplayName("incluye la sucursal a la que pertenece cada producto")
        void incluyeLaSucursalPropietaria() {
            Sucursal norte = Sucursal.crear("Sucursal Norte");
            Franquicia franquicia = Franquicia.crear("Cafes del Valle")
                    .agregarSucursal(norte)
                    .agregarProducto(norte.id(), Producto.crear("Cafe", 10));

            assertThat(franquicia.productosConMayorStockPorSucursal())
                    .singleElement()
                    .extracting(ProductoDestacado::sucursalId)
                    .isEqualTo(norte.id());
        }

        @Test
        @DisplayName("aplica el desempate por nombre dentro de cada sucursal")
        void desempataDentroDeCadaSucursal() {
            Sucursal norte = Sucursal.crear("Sucursal Norte");
            Franquicia franquicia = Franquicia.crear("Cafes del Valle")
                    .agregarSucursal(norte)
                    .agregarProducto(norte.id(), Producto.crear("Zanahoria", 50))
                    .agregarProducto(norte.id(), Producto.crear("Aguacate", 50));

            assertThat(franquicia.productosConMayorStockPorSucursal())
                    .singleElement()
                    .extracting(destacado -> destacado.producto().nombre())
                    .isEqualTo("Aguacate");
        }
    }
}
