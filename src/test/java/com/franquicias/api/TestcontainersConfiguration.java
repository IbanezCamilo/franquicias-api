package com.franquicias.api;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Mongo efimero para los tests de integracion y para el arranque local con
 * {@link TestFranquiciasApiApplication}.
 *
 * <p>La imagen se fija a una version concreta en lugar de {@code latest} para que
 * una actualizacion del upstream no cambie el resultado de la build sin avisar.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    private static final DockerImageName IMAGEN = DockerImageName.parse("mongo:8.0");

    @Bean
    @ServiceConnection
    MongoDBContainer mongoDbContainer() {
        return new MongoDBContainer(IMAGEN);
    }
}
