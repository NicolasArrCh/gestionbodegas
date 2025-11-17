package com.c3.gestionbodegas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAwareImpl")
@EnableAsync
public class GestionbodegasApplication extends SpringBootServletInitializer {

    /**
     * ✅ Este método es necesario para desplegar en Tomcat externo
     */
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(GestionbodegasApplication.class);
    }

    /**
     * Este método permite ejecutar la aplicación de forma standalone
     */
    public static void main(String[] args) {
        SpringApplication.run(GestionbodegasApplication.class, args);
    }
}