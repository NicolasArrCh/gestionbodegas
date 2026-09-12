package com.c3.gestionbodegas.dto.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.c3.gestionbodegas.dto.bodega.BodegaResponseDTO;
import com.c3.gestionbodegas.entities.Bodega;

@Component
public class BodegaMapper {

    public BodegaResponseDTO toDTO(Bodega bodega) {
        if (bodega == null) return null;
        return BodegaResponseDTO.builder()
                .id(bodega.getId())
                .nombre(bodega.getNombre())
                .ubicacion(bodega.getUbicacion())
                .capacidad(bodega.getCapacidad())
                .encargadoId(bodega.getEncargado() != null ? bodega.getEncargado().getId() : null)
                .encargadoNombre(bodega.getEncargado() != null ? bodega.getEncargado().getNombreCompleto() : null)
                .encargadoUsername(bodega.getEncargado() != null ? bodega.getEncargado().getUsername() : null)
                .fechaCreacion(bodega.getFechaCreacion())
                .fechaModificacion(bodega.getFechaModificacion())
                .build();
    }

    public List<BodegaResponseDTO> toDTOList(List<Bodega> bodegas) {
        if (bodegas == null) return Collections.emptyList();
        return bodegas.stream().map(this::toDTO).toList();
    }
}
