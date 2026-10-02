package com.franquicias.api.domain.model;

import com.franquicias.api.domain.exception.NombreDuplicadoException;
import com.franquicias.api.domain.exception.RecursoNoEncontradoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Sucursal")
class SucursalTest {

    @Test
    @DisplayName("se crea sin productos")
    void seCreaVacia() {
        Sucursal sucursal = Sucursal.crear("Sucursal Norte");

        assertThat(sucursal.id()).isNotNull();
        assertThat(sucursal.productos()).isEmpty();
    }

    @Test
    @DisplayName("la lista de productos es inmutable desde fuera")
    void listaDeProductosInmutable() {
        Sucursal sucursal = Sucursal.crear("Sucursal Norte");
        Producto cafe = Producto.crear("Cafe", 1);

        assertThatThrownBy(() -> sucursal.productos().add(cafe))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Nested
    @DisplayName("agregarProducto")
    class AgregarProducto {

        @Test
        @DisplayName("incorpora el producto sin mutar la sucursal original")
        void incorporaElProducto() {
            Sucursal original = Sucursal.crear("Sucursal Norte");

            Sucursal conProducto = original.agregarProducto(Producto.crear("Cafe", 10));

            assertThat(conProducto.productos()).hasSize(1);
            assertThat(original.productos()).as("el original no se altera").isEmpty();
        }

        @Test
        @DisplayName("rechaza un nombre ya presente aunque difiera en mayusculas")
        void rechazaNombreDuplicado() {
            Sucursal sucursal = Sucursal.crear("Sucursal Norte")
                    .agregarProducto(Producto.crear("Cafe Molido", 10));
            Producto duplicado = Producto.crear("  cafe molido ", 5);

            assertThatThrownBy(() -> sucursal.agregarProducto(duplicado))
                    .isInstanceOf(NombreDuplicadoException.class)
                    .hasMessageContaining("cafe molido");
        }
    }

    @Nested
    @DisplayName("eliminarProducto")
    class EliminarProducto {

        @Test
        @DisplayName("quita solo el producto indicado")
        void quitaSoloElIndicado() {
            Producto cafe = Producto.crear("Cafe", 10);
            Producto te = Producto.crear("Te", 5);
            Sucursal sucursal = Sucursal.crear("Sucursal Norte")
                    .agregarProducto(cafe)
                    .agregarProducto(te);

            Sucursal resultado = sucursal.eliminarProducto(cafe.id());

            assertThat(resultado.productos()).containsExactly(te);
        }

        @Test
        @DisplayName("falla si el producto no existe")
        void fallaSiNoExiste() {
            Sucursal sucursal = Sucursal.crear("Sucursal Norte");
            UUID inexistente = UUID.randomUUID();

            assertThatThrownBy(() -> sucursal.eliminarProducto(inexistente))
                    .isInstanceOf(RecursoNoEncontradoException.class)
                    .hasMessageContaining(inexistente.toString());
        }
    }

    @Nested
    @DisplayName("actualizarStock")
    class ActualizarStock {

        @Test
        @DisplayName("cambia el stock del producto indicado y deja el resto intacto")
        void cambiaElStock() {
            Producto cafe = Producto.crear("Cafe", 10);
            Producto te = Producto.crear("Te", 5);
            Sucursal sucursal = Sucursal.crear("Sucursal Norte")
                    .agregarProducto(cafe)
                    .agregarProducto(te);

            Sucursal resultado = sucursal.actualizarStock(cafe.id(), 99);

            assertThat(resultado.exigirProducto(cafe.id()).stock()).isEqualTo(99);
            assertThat(resultado.exigirProducto(te.id()).stock()).isEqualTo(5);
        }

        @Test
        @DisplayName("falla si el producto no existe")
        void fallaSiNoExiste() {
            Sucursal sucursal = Sucursal.crear("Sucursal Norte");
            UUID inexistente = UUID.randomUUID();

            assertThatThrownBy(() -> sucursal.actualizarStock(inexistente, 1))
                    .isInstanceOf(RecursoNoEncontradoException.class);
        }
    }

    @Nested
    @DisplayName("renombrarProducto")
    class RenombrarProducto {

        @Test
        @DisplayName("aplica el nuevo nombre conservando la identidad")
        void aplicaElNuevoNombre() {
            Producto cafe = Producto.crear("Cafe", 10);
            Sucursal sucursal = Sucursal.crear("Sucursal Norte").agregarProducto(cafe);

            Sucursal resultado = sucursal.renombrarProducto(cafe.id(), "Cafe en Grano");

            assertThat(resultado.exigirProducto(cafe.id()).nombre()).isEqualTo("Cafe en Grano");
        }

        @Test
        @DisplayName("permite renombrar un producto a su propio nombre")
        void permiteSuPropioNombre() {
            Producto cafe = Producto.crear("Cafe", 10);
            Sucursal sucursal = Sucursal.crear("Sucursal Norte").agregarProducto(cafe);

            Sucursal resultado = sucursal.renombrarProducto(cafe.id(), "CAFE");

            assertThat(resultado.exigirProducto(cafe.id()).nombre()).isEqualTo("CAFE");
        }

        @Test
        @DisplayName("rechaza el nombre de otro producto de la misma sucursal")
        void rechazaElNombreDeOtro() {
            Producto cafe = Producto.crear("Cafe", 10);
            Producto te = Producto.crear("Te", 5);
            Sucursal sucursal = Sucursal.crear("Sucursal Norte")
                    .agregarProducto(cafe)
                    .agregarProducto(te);

            assertThatThrownBy(() -> sucursal.renombrarProducto(te.id(), "Cafe"))
                    .isInstanceOf(NombreDuplicadoException.class);
        }
    }

    @Nested
    @DisplayName("productoConMayorStock")
    class ProductoConMayorStock {

        @Test
        @DisplayName("esta vacio si la sucursal no tiene productos")
        void vacioSinProductos() {
            assertThat(Sucursal.crear("Sucursal Norte").productoConMayorStock()).isEmpty();
        }

        @Test
        @DisplayName("devuelve el de mayor stock")
        void devuelveElDeMayorStock() {
            Sucursal sucursal = Sucursal.crear("Sucursal Norte")
                    .agregarProducto(Producto.crear("Cafe", 10))
                    .agregarProducto(Producto.crear("Te", 80))
                    .agregarProducto(Producto.crear("Azucar", 30));

            assertThat(sucursal.productoConMayorStock())
                    .map(Producto::nombre)
                    .contains("Te");
        }

        @Test
        @DisplayName("ante un empate devuelve el primero por nombre ascendente")
        void desempataPorNombre() {
            Sucursal sucursal = Sucursal.crear("Sucursal Norte")
                    .agregarProducto(Producto.crear("Zanahoria", 50))
                    .agregarProducto(Producto.crear("Aguacate", 50))
                    .agregarProducto(Producto.crear("Manzana", 50));

            assertThat(sucursal.productoConMayorStock())
                    .map(Producto::nombre)
                    .contains("Aguacate");
        }

        @Test
        @DisplayName("el desempate no depende del orden de insercion")
        void desempateEstable() {
            Sucursal unOrden = Sucursal.crear("Sucursal Norte")
                    .agregarProducto(Producto.crear("Aguacate", 50))
                    .agregarProducto(Producto.crear("Zanahoria", 50));
            Sucursal ordenInverso = Sucursal.crear("Sucursal Norte")
                    .agregarProducto(Producto.crear("Zanahoria", 50))
                    .agregarProducto(Producto.crear("Aguacate", 50));

            assertThat(unOrden.productoConMayorStock().orElseThrow().nombre())
                    .isEqualTo(ordenInverso.productoConMayorStock().orElseThrow().nombre())
                    .isEqualTo("Aguacate");
        }

        @Test
        @DisplayName("considera los productos con stock cero cuando son los unicos")
        void consideraStockCero() {
            Sucursal sucursal = Sucursal.crear("Sucursal Norte")
                    .agregarProducto(Producto.crear("Cafe", 0));

            assertThat(sucursal.productoConMayorStock()).isPresent();
        }
    }
}
