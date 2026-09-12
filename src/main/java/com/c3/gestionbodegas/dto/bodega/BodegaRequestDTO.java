package com.c3.gestionbodegas.dto.bodega;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

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
public class BodegaRequestDTO {

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    private String nombre;

    @NotBlank(message = "La ubicación no puede estar vacía")
    @Size(max = 150, message = "La ubicación no puede tener más de 150 caracteres")
    private String ubicacion;

    @NotNull(message = "La capacidad es requerida")
    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    private Integer capacidad;

    private Integer encargadoId;

    @JsonProperty("encargado")
    public void unpackEncargado(Map<String, Object> encargado) {
        if (encargado != null && encargado.containsKey("id")) {
            Object idVal = encargado.get("id");
            if (idVal instanceof Number) {
                this.encargadoId = ((Number) idVal).intValue();
            }
        }
    }
}
