package com.c3.gestionbodegas.listeners;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
    private ThreadLocal<Boolean> operacionFallida = new ThreadLocal<>();

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
        // No auditar entidades de Auditoria ni DetalleMovimiento
        if (entity instanceof Auditoria) return;
        if (entity instanceof com.c3.gestionbodegas.entities.DetalleMovimiento) return;
        
        // Solo auditar si la transacción fue exitosa
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        registrarAuditoria(Auditoria.TipoOperacion.INSERT, entity, null);
                    }
                }
            );
        } else {
            registrarAuditoria(Auditoria.TipoOperacion.INSERT, entity, null);
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
        if (entity instanceof com.c3.gestionbodegas.entities.DetalleMovimiento) return;
        
        // Solo auditar si la transacción fue exitosa
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            final String anterior = valorAnterior.get() != null ? valorAnterior.get() : "{}";
            
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        registrarAuditoria(Auditoria.TipoOperacion.UPDATE, entity, anterior);
                        valorAnterior.remove();
                    }
                    
                    @Override
                    public void afterCompletion(int status) {
                        valorAnterior.remove();
                    }
                }
            );
        } else {
            registrarAuditoria(Auditoria.TipoOperacion.UPDATE, entity, valorAnterior.get());
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
        if (entity instanceof com.c3.gestionbodegas.entities.DetalleMovimiento) return;
        
        // Solo auditar si la transacción fue exitosa
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            final String anterior = valorAnterior.get() != null ? valorAnterior.get() : "{}";
            
            TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronizationAdapter() {
                    @Override
                    public void afterCommit() {
                        registrarAuditoria(Auditoria.TipoOperacion.DELETE, entity, anterior);
                        valorAnterior.remove();
                    }
                    
                    @Override
                    public void afterCompletion(int status) {
                        valorAnterior.remove();
                    }
                }
            );
        } else {
            registrarAuditoria(Auditoria.TipoOperacion.DELETE, entity, valorAnterior.get());
            valorAnterior.remove();
        }
    }

    /**
     * Método centralizado para registrar auditoría
     */
    private void registrarAuditoria(Auditoria.TipoOperacion tipo, Object entity, String valorAnt) {
        try {
            String valorNuevo = null;
            if (tipo != Auditoria.TipoOperacion.DELETE) {
                valorNuevo = objectMapper.writeValueAsString(entity);
            }
            
            Usuario usuario = obtenerUsuarioActual();
            
            if (usuario == null) {
                System.err.println("⚠️ No se pudo obtener usuario para auditoría");
                return;
            }
            
            auditoriaAsyncService.guardarAuditoriaAsync(
                tipo, 
                usuario,
                entity.getClass().getSimpleName(), 
                valorAnt, 
                valorNuevo
            );
            
            System.out.println("✅ Auditoría " + tipo + " registrada para: " + entity.getClass().getSimpleName());
            
        } catch (Exception e) {
            System.err.println("❌ Error al registrar auditoría: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtiene el usuario autenticado actual
     */
    private Usuario obtenerUsuarioActual() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication != null && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal())) {
                
                String username = authentication.getName();
                return usuarioRepository.findByUsername(username)
                        .orElseGet(this::obtenerUsuarioSistema);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error al obtener usuario actual: " + e.getMessage());
        }
        
        return obtenerUsuarioSistema();
    }
    
    /**
     * Usuario por defecto cuando no hay autenticación
     */
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