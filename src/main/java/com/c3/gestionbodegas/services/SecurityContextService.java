package com.c3.gestionbodegas.services;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio centralizado para resolver el usuario autenticado actual
 * desde el SecurityContext de Spring Security.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SecurityContextService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Obtiene el usuario autenticado del SecurityContext.
     * Si no hay usuario autenticado o es anónimo, retorna el usuario "sistema" (ID 1).
     */
    public Usuario obtenerUsuarioActual() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                return usuarioRepository.findByUsername(auth.getName())
                        .orElseGet(this::obtenerUsuarioSistema);
            }
        } catch (Exception e) {
            log.warn("Error obteniendo usuario actual: {}", e.getMessage());
        }
        return obtenerUsuarioSistema();
    }

    /**
     * Obtiene o crea una representación en memoria del usuario del sistema (ID 1).
     */
    public Usuario obtenerUsuarioSistema() {
        try {
            return usuarioRepository.findById(1).orElseGet(() -> {
                Usuario temp = new Usuario();
                temp.setId(1);
                temp.setUsername("sistema");
                return temp;
            });
        } catch (Exception e) {
            Usuario temp = new Usuario();
            temp.setId(1);
            temp.setUsername("sistema");
            return temp;
        }
    }

    /**
     * Retorna el username autenticado actual o "sistema" si no hay sesión.
     */
    public String obtenerUsernameActual() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                return auth.getName();
            }
        } catch (Exception e) {
            log.warn("Error obteniendo username actual: {}", e.getMessage());
        }
        return "sistema";
    }
}
