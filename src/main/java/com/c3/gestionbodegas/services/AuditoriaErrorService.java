package com.c3.gestionbodegas.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.Bodega;
import com.c3.gestionbodegas.entities.IntentoFallido;
import com.c3.gestionbodegas.entities.Producto;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Servicio para registrar intentos fallidos y errores
 * NO en la tabla de auditoría, sino en intentos_fallidos
 */
@Service
public class AuditoriaErrorService {

    @Autowired
    private IntentoFallidoService intentoFallidoService;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Registra un error de operación en la tabla de intentos_fallidos
     */
    public void registrarError(
            String tipoOperacion, 
            String entidad,
            String razonError,
            Object datosIntentados) {
        
        try {
            Usuario usuario = obtenerUsuarioActual();
            
            // Convertir datos a JSON para detalles adicionales
            String detallesJson = null;
            if (datosIntentados != null) {
                detallesJson = objectMapper.writeValueAsString(datosIntentados);
            }
            
            // Determinar tipo de movimiento según la operación
            IntentoFallido.TipoMovimiento tipo = determinarTipoMovimiento(tipoOperacion);
            
            // Extraer producto y bodegas si están disponibles
            Producto producto = null;
            Bodega bodegaOrigen = null;
            Bodega bodegaDestino = null;
            Integer cantidad = 0;
            
            // Si datosIntentados es un objeto complejo, extraer información
            if (datosIntentados != null) {
                try {
                    // Intentar extraer campos comunes
                    var map = objectMapper.convertValue(datosIntentados, java.util.Map.class);
                    
                    if (map.containsKey("cantidad")) {
                        cantidad = ((Number) map.get("cantidad")).intValue();
                    }
                    
                    // Aquí podrías extraer más información si es necesario
                } catch (Exception e) {
                    // Si no se puede convertir, usar valores por defecto
                }
            }
            
            // Crear producto temporal para el registro
            if (producto == null) {
                producto = new Producto();
                producto.setId(0); // ID temporal
                producto.setNombre("N/A");
            }
            
            // Registrar el intento fallido
            intentoFallidoService.registrarIntentoFallido(
                tipo,
                razonError + " | Operación: " + tipoOperacion + " | Entidad: " + entidad,
                usuario,
                producto,
                bodegaOrigen,
                bodegaDestino,
                cantidad,
                detallesJson
            );
            
            System.out.println("⚠️ Error registrado: " + razonError);
            
        } catch (Exception e) {
            System.err.println("❌ Error al registrar error en auditoría: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Determina el tipo de movimiento según la operación
     */
    private IntentoFallido.TipoMovimiento determinarTipoMovimiento(String operacion) {
        if (operacion == null) return IntentoFallido.TipoMovimiento.ENTRADA;
        
        if (operacion.toUpperCase().contains("ENTRADA")) {
            return IntentoFallido.TipoMovimiento.ENTRADA;
        } else if (operacion.toUpperCase().contains("SALIDA")) {
            return IntentoFallido.TipoMovimiento.SALIDA;
        } else if (operacion.toUpperCase().contains("TRANSFERENCIA")) {
            return IntentoFallido.TipoMovimiento.TRANSFERENCIA;
        }
        
        return IntentoFallido.TipoMovimiento.ENTRADA;
    }
    
    /**
     * Obtiene el usuario actual del contexto de seguridad
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
            System.err.println("⚠️ Error obteniendo usuario actual: " + e.getMessage());
        }
        
        return obtenerUsuarioSistema();
    }
    
    /**
     * Usuario por defecto del sistema
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