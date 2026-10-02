package com.franquicias.api.domain.validation;

import com.franquicias.api.domain.exception.ReglaDeNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Nombres")
class NombresTest {

    @Nested
    @DisplayName("normalizarYValidar")
    class NormalizarYValidar {

        @Test
        @DisplayName("recorta los espacios de los extremos")
        void recortaEspacios() {
            assertThat(Nombres.normalizarYValidar("nombre", "  Sucursal Norte  "))
                    .isEqualTo("Sucursal Norte");
        }

        @Test
        @DisplayName("conserva los espacios interiores")
        void conservaEspaciosInteriores() {
            assertThat(Nombres.normalizarYValidar("nombre", "Cafe de la  Esquina"))
                    .isEqualTo("Cafe de la  Esquina");
        }

        @ParameterizedTest(name = "valor = \"{0}\"")
        @NullAndEmptySource
        @ValueSource(strings = {"   ", "\t", "\n"})
        @DisplayName("rechaza nulos y valores en blanco")
        void rechazaVacios(String valor) {
            assertThatThrownBy(() -> Nombres.normalizarYValidar("nombre de la sucursal", valor))
                    .isInstanceOf(ReglaDeNegocioException.class)
                    .hasMessageContaining("El nombre de la sucursal es obligatorio");
        }

        @Test
        @DisplayName("rechaza nombres de un solo caracter")
        void rechazaDemasiadoCorto() {
            assertThatThrownBy(() -> Nombres.normalizarYValidar("nombre", "A"))
                    .isInstanceOf(ReglaDeNegocioException.class)
                    .hasMessageContaining("entre 2 y 100 caracteres");
        }

        @Test
        @DisplayName("rechaza nombres de mas de 100 caracteres")
        void rechazaDemasiadoLargo() {
            assertThatThrownBy(() -> Nombres.normalizarYValidar("nombre", "x".repeat(101)))
                    .isInstanceOf(ReglaDeNegocioException.class)
                    .hasMessageContaining("entre 2 y 100 caracteres");
        }

        @Test
        @DisplayName("acepta exactamente 100 caracteres")
        void aceptaElLimiteSuperior() {
            String limite = "x".repeat(100);
            assertThat(Nombres.normalizarYValidar("nombre", limite)).isEqualTo(limite);
        }

        @Test
        @DisplayName("valida la longitud despues de recortar, no antes")
        void validaLongitudDespuesDeRecortar() {
            assertThatThrownBy(() -> Nombres.normalizarYValidar("nombre", "   A   "))
                    .isInstanceOf(ReglaDeNegocioException.class);
        }
    }

    @Nested
    @DisplayName("sonEquivalentes")
    class SonEquivalentes {

        @Test
        @DisplayName("ignora mayusculas y espacios de los extremos")
        void ignoraMayusculasYEspacios() {
            assertThat(Nombres.sonEquivalentes("Sucursal Norte", " sucursal norte ")).isTrue();
        }

        @Test
        @DisplayName("distingue nombres realmente diferentes")
        void distingueDiferentes() {
            assertThat(Nombres.sonEquivalentes("Sucursal Norte", "Sucursal Sur")).isFalse();
        }
    }
}
