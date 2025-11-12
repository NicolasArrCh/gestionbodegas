package com.c3.gestionbodegas.dto;

import java.time.LocalDateTime;

import com.c3.gestionbodegas.entities.Bodega;
import com.c3.gestionbodegas.entities.Usuario;
import com.c3.gestionbodegas.entities.MovimientoInventario.TipoMovimiento;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class MovimientoInventarioDTO {
    private Integer id;
    
    private LocalDateTime fecha = LocalDateTime.now();

    private TipoMovimiento tipo;

    private Usuario usuario;

    private Bodega bodegaOrigen;

    private Bodega bodegaDestino;
}
