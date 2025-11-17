package com.c3.gestionbodegas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI gestionBodegasOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API Gestión de Bodegas")
                        .description("Documentación de la API para el sistema de gestión de bodegas.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo de Desarrollo C3")
                                .email("soporte@c3.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))
                );
    }
}

