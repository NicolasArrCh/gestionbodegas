package com.c3.gestionbodegas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class BodegaDTO {
    
    private Long id;

    @NotBlank(message = "El nombre de la bodega no puede estar vacío")
    @Size(max = 100, message = "El nombre no debe exceder los 100 caracteres")
    private String nombre;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 150, message = "La ubicación no debe exceder los 150 caracteres")
    private String ubicacion;

    @Min(value = 0, message = "La capacidad no puede ser negativa")
    private Integer capacidad;

    @NotBlank(message = "Debe especificarse un encargado")
    @Size(max = 100, message = "El nombre del encargado no debe exceder los 100 caracteres")
    private String encargado;

}
