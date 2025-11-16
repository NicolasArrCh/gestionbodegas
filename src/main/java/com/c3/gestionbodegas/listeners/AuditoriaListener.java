package com.c3.gestionbodegas.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.c3.gestionbodegas.entities.Auditoria;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.repository.UsuarioRepository;
import com.c3.gestionbodegas.services.AuditoriaAsyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreRemove;
import jakarta.persistence.PreUpdate;

@Component
public class AuditoriaListener {

    private static AuditoriaAsyncService auditoriaAsyncService;
    private static UsuarioRepository usuarioRepository;
    private static ObjectMapper objectMapper;
    
    private ThreadLocal<String> valorAnterior = new ThreadLocal<>();

    @Autowired
    public void init(@Lazy AuditoriaAsyncService auditoriaAsyncService,
                     @Lazy UsuarioRepository usuarioRepository) {
        AuditoriaListener.auditoriaAsyncService = auditoriaAsyncService;
        AuditoriaListener.usuarioRepository = usuarioRepository;
        AuditoriaListener.objectMapper = new ObjectMapper();
        AuditoriaListener.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostPersist
    public void postPersist(Object entity) {
        if (entity instanceof Auditoria) return;
        if (entity instanceof com.c3.gestionbodegas.entities.DetalleMovimiento) return;  // ← Excluir DetalleMovimiento
        
        try {
            String valorNuevo = objectMapper.writeValueAsString(entity);
            Usuario usuario = obtenerUsuarioActual();
            
            if (usuario == null) return;
            
            auditoriaAsyncService.guardarAuditoriaAsync(
                Auditoria.TipoOperacion.INSERT, usuario,
                entity.getClass().getSimpleName(), null, valorNuevo
            );
        } catch (Exception e) {
            System.err.println("❌ Error al auditar INSERT: " + e.getMessage());
        }
    }

    @PreUpdate
    public void preUpdate(Object entity) {
        if (entity instanceof Auditoria) return;
        
        try {
            String valor = objectMapper.writeValueAsString(entity);
            valorAnterior.set(valor);
        } catch (Exception e) {
            valorAnterior.set("{}");
        }
    }

    @PostUpdate
    public void postUpdate(Object entity) {
        if (entity instanceof Auditoria) return;
        if (entity instanceof com.c3.gestionbodegas.entities.DetalleMovimiento) return;  // ← Excluir DetalleMovimiento
        
        try {
            String valorNuevo = objectMapper.writeValueAsString(entity);
            Usuario usuario = obtenerUsuarioActual();
            
            if (usuario == null) {
                valorAnterior.remove();
                return;
            }
            
            String anterior = valorAnterior.get() != null ? valorAnterior.get() : "{}";
            
            auditoriaAsyncService.guardarAuditoriaAsync(
                Auditoria.TipoOperacion.UPDATE, usuario,
                entity.getClass().getSimpleName(), anterior, valorNuevo
            );
            
            valorAnterior.remove();
        } catch (Exception e) {
            System.err.println("❌ Error al auditar UPDATE: " + e.getMessage());
            e.printStackTrace();
            valorAnterior.remove();
        }
    }

    @PreRemove
    public void preRemove(Object entity) {
        if (entity instanceof Auditoria) return;
        
        try {
            String valor = objectMapper.writeValueAsString(entity);
            valorAnterior.set(valor);
        } catch (Exception e) {
            valorAnterior.set("{}");
        }
    }

    @PostRemove
    public void postRemove(Object entity) {
        if (entity instanceof Auditoria) return;
        if (entity instanceof com.c3.gestionbodegas.entities.DetalleMovimiento) return;  // ← Excluir DetalleMovimiento
        
        try {
            Usuario usuario = obtenerUsuarioActual();
            
            if (usuario == null) {
                valorAnterior.remove();
                return;
            }
            
            String anterior = valorAnterior.get() != null ? valorAnterior.get() : "{}";
            
            auditoriaAsyncService.guardarAuditoriaAsync(
                Auditoria.TipoOperacion.DELETE, usuario,
                entity.getClass().getSimpleName(), anterior, null
            );
            
            valorAnterior.remove();
        } catch (Exception e) {
            System.err.println("❌ Error al auditar DELETE: " + e.getMessage());
            e.printStackTrace();
            valorAnterior.remove();
        }
    }

    private Usuario obtenerUsuarioActual() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
                
                String username = authentication.getName();
                return usuarioRepository.findByUsername(username).orElseGet(() -> obtenerUsuarioSistema());
            }
        } catch (Exception e) {
            System.err.println("Error al obtener usuario actual: " + e.getMessage());
        }
        
        return obtenerUsuarioSistema();
    }
    
    private Usuario obtenerUsuarioSistema() {
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
}