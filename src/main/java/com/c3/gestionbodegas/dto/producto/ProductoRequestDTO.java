package com.c3.gestionbodegas.dto.producto;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequestDTO {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    private String nombre;

    @NotBlank(message = "La categoría no puede estar vacía")
    @Size(max = 100, message = "La categoría no puede tener más de 100 caracteres")
    private String categoria;

    @NotNull(message = "El stock es requerido")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @NotNull(message = "El precio es requerido")
    @DecimalMin(value = "0.0", inclusive = false, message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    private Integer bodegaId;

    @JsonProperty("bodega")
    public void unpackBodega(Map<String, Object> bodega) {
        if (bodega != null && bodega.containsKey("id")) {
            Object idVal = bodega.get("id");
            if (idVal instanceof Number) {
                this.bodegaId = ((Number) idVal).intValue();
            }
        }
    }
}
