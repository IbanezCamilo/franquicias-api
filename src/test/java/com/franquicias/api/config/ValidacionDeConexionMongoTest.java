package com.franquicias.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Validacion de la conexion a MongoDB al arrancar")
class ValidacionDeConexionMongoTest {

    private static final String PROPIEDAD = "spring.mongodb.uri";

    private ValidacionDeConexionMongo validacion;
    private MockEnvironment entorno;
    private ApplicationEnvironmentPreparedEvent evento;

    @BeforeEach
    void preparar() {
        validacion = new ValidacionDeConexionMongo();
        entorno = new MockEnvironment();
        evento = mock(ApplicationEnvironmentPreparedEvent.class);
        when(evento.getEnvironment()).thenReturn(entorno);
    }

    @Test
    @DisplayName("acepta una cadena mongodb+srv valida")
    void aceptaSrv() {
        entorno.setProperty(PROPIEDAD, "mongodb+srv://usuario:clave@host/franquicias");

        assertThatCode(() -> validacion.onApplicationEvent(evento)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("acepta una cadena mongodb estandar, que es la de desarrollo local")
    void aceptaEstandar() {
        entorno.setProperty(PROPIEDAD, "mongodb://localhost:27017/franquicias");

        assertThatCode(() -> validacion.onApplicationEvent(evento)).doesNotThrowAnyException();
    }

    @ParameterizedTest(name = "valor = \"{0}\"")
    @ValueSource(strings = {"", "   "})
    @DisplayName("rechaza un valor vacio y explica que el valor por defecto no se aplica")
    void rechazaVacio(String valor) {
        // El caso que tumbo el primer despliegue: la variable existe pero sin valor,
        // de modo que ${MONGODB_URI:defecto} resuelve a cadena vacia.
        entorno.setProperty(PROPIEDAD, valor);

        assertThatThrownBy(() -> validacion.onApplicationEvent(evento))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("MONGODB_URI")
                .hasMessageContaining("el valor por defecto NO se aplica");
    }

    @Test
    @DisplayName("rechaza un valor que no es una cadena de conexion")
    void rechazaFormatoInvalido() {
        entorno.setProperty(PROPIEDAD, "\"mongodb+srv://host/db\"");

        assertThatThrownBy(() -> validacion.onApplicationEvent(evento))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("formato invalido")
                .hasMessageContaining("entre comillas");
    }

    @Test
    @DisplayName("al informar del formato invalido no revela las credenciales")
    void noFiltraCredenciales() {
        entorno.setProperty(PROPIEDAD, "http://usuario:SECRETO_MUY_LARGO@host/db");

        assertThatThrownBy(() -> validacion.onApplicationEvent(evento))
                .isInstanceOf(IllegalStateException.class)
                .satisfies(error -> assertThat(error.getMessage()).doesNotContain("SECRETO_MUY_LARGO"));
    }
}
