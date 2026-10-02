package com.franquicias.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Metadatos de la documentacion OpenAPI que springdoc expone en Swagger UI. */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfiguration {

    @Bean
    OpenAPI definicionDeLaApi() {
        return new OpenAPI().info(new Info()
                .title("API de Franquicias")
                .version("v1")
                .description("""
                        API reactiva para gestionar franquicias, sus sucursales y los productos \
                        ofertados en cada una.

                        Todos los errores se devuelven como `application/problem+json` \
                        siguiendo la RFC 9457.""")
                .contact(new Contact().name("Camilo Pastrana").url("https://github.com/IbanezCamilo"))
                .license(new License().name("MIT")));
    }
}
