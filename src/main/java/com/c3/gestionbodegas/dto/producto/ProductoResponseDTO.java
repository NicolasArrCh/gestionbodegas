package com.c3.gestionbodegas.dto.producto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {

    private Integer id;
    private String nombre;
    private String categoria;
    private Integer stock;
    private BigDecimal precio;
    private Integer bodegaId;
    private String bodegaNombre;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaModificacion;

    /**
     * Objeto anidado para mantener compatibilidad con clientes frontend existentes (ej. producto.bodega?.nombre).
     */
    public BodegaInfo getBodega() {
        if (bodegaId == null && bodegaNombre == null) return null;
        return new BodegaInfo(bodegaId, bodegaNombre);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BodegaInfo {
        private Integer id;
        private String nombre;
    }
}
