package com.c3.gestionbodegas.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Implementación de AuditorAware para JPA Auditing.
 * Proporciona el nombre del usuario actual para los campos @CreatedBy y @LastModifiedBy.
 */
@Component
public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of(authentication.getName());
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error obteniendo auditor actual: " + e.getMessage());
        }

        // Si no hay usuario autenticado, retornar "sistema" por defecto
        return Optional.of("sistema");
    }
}