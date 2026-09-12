package com.c3.gestionbodegas.dto.bodega;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BodegaResumenDTO {

    private Integer id;
    private String nombre;
    private String ubicacion;
    private Integer capacidad;
    private Long stockTotal;
}
