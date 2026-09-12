package com.c3.gestionbodegas.dto.movimiento;

import java.util.List;

import com.c3.gestionbodegas.entities.MovimientoInventario;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoRequestDTO {

    @NotNull(message = "El tipo de movimiento es requerido")
    private MovimientoInventario.TipoMovimiento tipo;

    private Integer bodegaOrigenId;
    private Integer bodegaDestinoId;

    @NotEmpty(message = "Debe incluir al menos un detalle de movimiento")
    @Valid
    private List<MovimientoDetalleDTO> detalles;
}
