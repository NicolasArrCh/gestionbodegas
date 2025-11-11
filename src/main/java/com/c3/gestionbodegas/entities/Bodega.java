package com.c3.gestionbodegas.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "bodegas")
@Data // Genera getters, setters, equals, hashCode y toString
@NoArgsConstructor // Constructor vacio
@AllArgsConstructor // Constructor con todos los campos
@Builder // Permite crear objetos con patrón Builder

public class Bodega {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre de la bodega no puede estar vacío")
    @Size(max = 100, message = "El nombre no debe exceder los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "La ubicación es obligatoria")
    @Size(max = 150, message = "La ubicación no debe exceder los 150 caracteres")
    @Column(nullable = false, length = 150)
    private String ubicacion;

    @Min(value = 0, message = "La capacidad no puede ser negativa")
    @Column(nullable = false)
    private Integer capacidad;

    @NotBlank(message = "Debe especificarse un encargado")
    @Size(max = 100, message = "El nombre del encargado no debe exceder los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String encargado;
    
}
