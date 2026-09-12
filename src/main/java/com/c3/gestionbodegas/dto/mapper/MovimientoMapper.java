package com.c3.gestionbodegas.dto.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.c3.gestionbodegas.dto.movimiento.MovimientoResponseDTO;
import com.c3.gestionbodegas.entities.DetalleMovimiento;
import com.c3.gestionbodegas.entities.MovimientoInventario;

@Component
public class MovimientoMapper {

    public MovimientoResponseDTO toResponseDTO(MovimientoInventario movimiento, List<DetalleMovimiento> detalles) {
        if (movimiento == null) return null;
        
        List<MovimientoResponseDTO.DetalleItemDTO> detallesDTO = detalles != null
                ? detalles.stream().map(d -> MovimientoResponseDTO.DetalleItemDTO.builder()
                        .id(d.getId())
                        .productoId(d.getProducto() != null ? d.getProducto().getId() : null)
                        .productoNombre(d.getProducto() != null ? d.getProducto().getNombre() : null)
                        .cantidad(d.getCantidad())
                        .build()).toList()
                : Collections.emptyList();

        return MovimientoResponseDTO.builder()
                .id(movimiento.getId())
                .fecha(movimiento.getFecha())
                .tipo(movimiento.getTipo())
                .usuarioId(movimiento.getUsuario() != null ? movimiento.getUsuario().getId() : null)
                .usuarioNombre(movimiento.getUsuario() != null ? movimiento.getUsuario().getNombreCompleto() : null)
                .bodegaOrigenId(movimiento.getBodegaOrigen() != null ? movimiento.getBodegaOrigen().getId() : null)
                .bodegaOrigenNombre(movimiento.getBodegaOrigen() != null ? movimiento.getBodegaOrigen().getNombre() : null)
                .bodegaDestinoId(movimiento.getBodegaDestino() != null ? movimiento.getBodegaDestino().getId() : null)
                .bodegaDestinoNombre(movimiento.getBodegaDestino() != null ? movimiento.getBodegaDestino().getNombre() : null)
                .detalles(detallesDTO)
                .build();
    }
}
