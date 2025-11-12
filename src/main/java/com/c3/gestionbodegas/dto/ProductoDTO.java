package com.c3.gestionbodegas.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ProductoDTO {
    private Integer id;

    @NotBlank(message = "El nombre de la bodega no puede estar vacío")
    @Size(max = 100, message = "El nombre no debe exceder los 100 caracteres")
    private String nombre;

    @NotBlank(message = "La categoría no puede estar vacía")
    @Size(max = 100, message = "La categoría no debe exceder los 100 caracteres")
    private String categoria;

    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    private BigDecimal precio;

}
