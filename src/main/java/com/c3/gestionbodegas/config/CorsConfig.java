package com.c3.gestionbodegas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Permitir orígenes (cambiar según necesidad)
        configuration.addAllowedOrigin("http://localhost:8080");
        configuration.addAllowedOrigin("http://localhost:3000");
        configuration.addAllowedOrigin("http://127.0.0.1:8080");
        
        // Permitir métodos HTTP
        configuration.addAllowedMethod("GET");
        configuration.addAllowedMethod("POST");
        configuration.addAllowedMethod("PUT");
        configuration.addAllowedMethod("DELETE");
        configuration.addAllowedMethod("OPTIONS");
        
        // Permitir headers
        configuration.addAllowedHeader("*");
        
        // Permitir el header Authorization (importante para JWT)
        configuration.addAllowedHeader("Authorization");
        configuration.addAllowedHeader("Content-Type");
        
        // Permitir cookies
        configuration.setAllowCredentials(true);
        
        // Máximo tiempo de caché de preflight
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}
