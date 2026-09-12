package com.c3.gestionbodegas.services;

import org.springframework.stereotype.Service;

import com.c3.gestionbodegas.entities.Bodega;
import com.c3.gestionbodegas.entities.IntentoFallido;
import com.c3.gestionbodegas.entities.Producto;
import com.c3.gestionbodegas.entities.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio para registrar intentos fallidos y errores
 * en la tabla intentos_fallidos.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditoriaErrorService {

    private final IntentoFallidoService intentoFallidoService;
    private final SecurityContextService securityContextService;
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
            Usuario usuario = securityContextService.obtenerUsuarioActual();
            
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
                    var map = objectMapper.convertValue(datosIntentados, java.util.Map.class);
                    if (map.containsKey("cantidad")) {
                        cantidad = ((Number) map.get("cantidad")).intValue();
                    }
                } catch (Exception e) {
                    log.debug("No se pudo extraer cantidad de datosIntentados: {}", e.getMessage());
                }
            }
            
            // Crear producto temporal para el registro si no existe
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
            
            log.warn("Error registrado en auditoría de errores: {}", razonError);
            
        } catch (Exception e) {
            log.error("Error al registrar error en auditoría: {}", e.getMessage(), e);
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
}