package com.c3.gestionbodegas.dto;

import com.c3.gestionbodegas.entities.MovimientoInventario;
import com.c3.gestionbodegas.entities.Producto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class DetalleMovimientoDTO {
    private Long id;

    private MovimientoInventario movimiento;

    private Producto producto;

    private Integer cantidad;
}
