package com.franquicias.api.config;

import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Comprueba la cadena de conexion a MongoDB antes de que arranque nada mas.
 *
 * <p>Motivo: la sintaxis {@code ${MONGODB_URI:valor-por-defecto}} aplica el valor por
 * defecto solo cuando la variable <em>no existe</em>. Si existe pero esta vacia, Spring
 * resuelve a cadena vacia y el driver falla con un
 * {@code IllegalArgumentException} enterrado bajo un centenar de lineas de traza, que
 * no dice en ningun momento cual es la variable culpable.
 *
 * <p>Es un escenario real, no hipotetico: ocurre al desplegar en un PaaS donde la
 * variable queda declarada sin valor. Esta validacion lo convierte en un mensaje de una
 * linea que nombra el problema y como arreglarlo.
 *
 * <p>Se registra como listener en {@code META-INF/spring.factories} para ejecutarse
 * antes de que se cree ningun bean.
 */
public class ValidacionDeConexionMongo
        implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static final String PROPIEDAD = "spring.mongodb.uri";
    private static final String VARIABLE = "MONGODB_URI";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent evento) {
        ConfigurableEnvironment entorno = evento.getEnvironment();
        String uri = entorno.getProperty(PROPIEDAD);

        if (uri == null || uri.isBlank()) {
            throw new IllegalStateException("""
                    La cadena de conexion a MongoDB esta vacia.

                    La variable de entorno %s existe pero no tiene valor. Ojo: cuando \
                    la variable esta definida y vacia, el valor por defecto NO se aplica.

                    Asigna la cadena completa, por ejemplo:
                      %s=mongodb+srv://usuario:password@host/franquicias?retryWrites=true&w=majority

                    O elimina la variable para usar el MongoDB local por defecto."""
                    .formatted(VARIABLE, VARIABLE));
        }

        if (!uri.startsWith("mongodb://") && !uri.startsWith("mongodb+srv://")) {
            throw new IllegalStateException("""
                    La cadena de conexion a MongoDB tiene un formato invalido.

                    %s debe empezar por 'mongodb://' o 'mongodb+srv://', y empieza por: %s

                    Causas habituales: se pego el valor entre comillas, incompleto, o con \
                    espacios o saltos de linea al principio."""
                    .formatted(VARIABLE, recorte(uri)));
        }
    }

    /** Muestra el principio del valor sin revelar las credenciales que vienen despues. */
    private static String recorte(String uri) {
        String inicio = uri.length() > 12 ? uri.substring(0, 12) : uri;
        return "'" + inicio + "...'";
    }
}
