package com.c3.gestionbodegas.config;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import com.c3.gestionbodegas.services.SecurityContextService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementación de AuditorAware para JPA Auditing.
 * Proporciona el nombre del usuario actual para los campos @CreatedBy y @LastModifiedBy.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuditorAwareImpl implements AuditorAware<String> {

    private final SecurityContextService securityContextService;

    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            return Optional.of(securityContextService.obtenerUsernameActual());
        } catch (Exception e) {
            log.warn("Error obteniendo auditor actual: {}", e.getMessage());
            return Optional.of("sistema");
        }
    }
}