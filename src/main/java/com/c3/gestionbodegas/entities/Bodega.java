package com.c3.gestionbodegas.entities;

import com.c3.gestionbodegas.listeners.AuditoriaListener;
import com.c3.gestionbodegas.model.Auditable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@EntityListeners(AuditoriaListener.class) // ✅ ACTIVAR AUDITORÍA
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bodega extends Auditable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "La ubicación no puede estar vacía")
    @Size(max = 150, message = "La ubicación no puede tener más de 150 caracteres")
    @Column(nullable = false, length = 150)
    private String ubicacion;

    @Min(value = 1, message = "La capacidad debe ser mayor a 0")
    @Column(nullable = false)
    private Integer capacidad;

    @ManyToOne
    @JoinColumn(name = "encargado_id", nullable = false)
    private Usuario encargado;
}