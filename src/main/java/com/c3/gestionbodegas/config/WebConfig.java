package com.c3.gestionbodegas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Servir archivos HTML desde resources/html
        registry.addResourceHandler("/html/**")
                .addResourceLocations("classpath:/html/");

        // Servir archivos CSS desde resources/css
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/css/");

        // Servir archivos JS desde resources/js
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/js/");

        // Servir index.html en la raíz
        registry.addResourceHandler("/")
                .addResourceLocations("classpath:/html/login.html");
    }
}
