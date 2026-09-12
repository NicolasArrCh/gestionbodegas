package com.c3.gestionbodegas.dto.movimiento;

import java.time.LocalDateTime;
import java.util.List;

import com.c3.gestionbodegas.entities.MovimientoInventario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoResponseDTO {

    private Integer id;
    private LocalDateTime fecha;
    private MovimientoInventario.TipoMovimiento tipo;
    private Integer usuarioId;
    private String usuarioNombre;
    private Integer bodegaOrigenId;
    private String bodegaOrigenNombre;
    private Integer bodegaDestinoId;
    private String bodegaDestinoNombre;
    private List<DetalleItemDTO> detalles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleItemDTO {
        private Integer id;
        private Integer productoId;
        private String productoNombre;
        private Integer cantidad;
    }
}
