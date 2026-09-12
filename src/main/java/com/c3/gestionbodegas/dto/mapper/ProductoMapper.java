package com.c3.gestionbodegas.dto.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.c3.gestionbodegas.dto.producto.ProductoResponseDTO;
import com.c3.gestionbodegas.entities.Producto;

@Component
public class ProductoMapper {

    public ProductoResponseDTO toDTO(Producto producto) {
        if (producto == null) return null;
        return ProductoResponseDTO.builder()
                .id(producto.getId())
                .nombre(producto.getNombre())
                .categoria(producto.getCategoria())
                .stock(producto.getStock())
                .precio(producto.getPrecio())
                .bodegaId(producto.getBodega() != null ? producto.getBodega().getId() : null)
                .bodegaNombre(producto.getBodega() != null ? producto.getBodega().getNombre() : null)
                .fechaCreacion(producto.getFechaCreacion())
                .fechaModificacion(producto.getFechaModificacion())
                .build();
    }

    public List<ProductoResponseDTO> toDTOList(List<Producto> productos) {
        if (productos == null) return Collections.emptyList();
        return productos.stream().map(this::toDTO).toList();
    }
}
