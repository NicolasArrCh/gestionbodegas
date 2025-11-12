package com.c3.gestionbodegas.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class AuditoriaDTO {

    private Integer id;

    private String nombre;

    private String ubicacion;

    private Integer capacidad;

    private String encargado;
}
