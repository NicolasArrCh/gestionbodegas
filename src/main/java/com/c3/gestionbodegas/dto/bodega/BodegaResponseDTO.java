package com.c3.gestionbodegas.dto.bodega;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BodegaResponseDTO {

    private Integer id;
    private String nombre;
    private String ubicacion;
    private Integer capacidad;
    private Integer encargadoId;
    private String encargadoNombre;
    private String encargadoUsername;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    /**
     * Objeto anidado para mantener compatibilidad con clientes existentes (ej. bodega.encargado?.username).
     */
    public EncargadoInfo getEncargado() {
        if (encargadoId == null && encargadoNombre == null && encargadoUsername == null) return null;
        return new EncargadoInfo(encargadoId, encargadoNombre, encargadoUsername);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EncargadoInfo {
        private Integer id;
        private String nombreCompleto;
        private String username;
    }
}
