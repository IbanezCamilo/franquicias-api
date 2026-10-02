package com.franquicias.api.domain.model;

import com.franquicias.api.domain.exception.ReglaDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Producto")
class ProductoTest {

    @Test
    @DisplayName("se crea con identidad propia y nombre normalizado")
    void seCreaConIdentidadPropia() {
        Producto producto = Producto.crear("  Cafe Molido  ", 10);

        assertThat(producto.id()).isNotNull();
        assertThat(producto.nombre()).isEqualTo("Cafe Molido");
        assertThat(producto.stock()).isEqualTo(10);
    }

    @Test
    @DisplayName("acepta stock cero: un producto agotado sigue existiendo")
    void aceptaStockCero() {
        assertThat(Producto.crear("Cafe Molido", 0).stock()).isZero();
    }

    @Test
    @DisplayName("rechaza stock negativo")
    void rechazaStockNegativo() {
        assertThatThrownBy(() -> Producto.crear("Cafe Molido", -1))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessage("El stock no puede ser negativo");
    }

    @Test
    @DisplayName("rechaza un nombre en blanco")
    void rechazaNombreEnBlanco() {
        assertThatThrownBy(() -> Producto.crear("   ", 5))
                .isInstanceOf(ReglaDeNegocioException.class)
                .hasMessageContaining("nombre del producto");
    }

    @Test
    @DisplayName("conStock devuelve una instancia nueva y conserva identidad y nombre")
    void conStockEsInmutable() {
        Producto original = Producto.crear("Cafe Molido", 10);

        Producto modificado = original.conStock(42);

        assertThat(modificado.stock()).isEqualTo(42);
        assertThat(modificado.id()).isEqualTo(original.id());
        assertThat(modificado.nombre()).isEqualTo(original.nombre());
        assertThat(original.stock()).as("el original no se altera").isEqualTo(10);
    }

    @Test
    @DisplayName("conStock rechaza un valor negativo")
    void conStockRechazaNegativo() {
        Producto producto = Producto.crear("Cafe Molido", 10);

        assertThatThrownBy(() -> producto.conStock(-5))
                .isInstanceOf(ReglaDeNegocioException.class);
    }

    @Test
    @DisplayName("renombrado conserva identidad y stock")
    void renombradoConservaIdentidad() {
        Producto original = Producto.crear("Cafe Molido", 10);

        Producto renombrado = original.renombrado("Cafe en Grano");

        assertThat(renombrado.id()).isEqualTo(original.id());
        assertThat(renombrado.nombre()).isEqualTo("Cafe en Grano");
        assertThat(renombrado.stock()).isEqualTo(10);
    }
}
