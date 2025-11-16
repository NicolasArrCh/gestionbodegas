package com.c3.gestionbodegas.entities;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "auditoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_operacion", nullable = false)
    private TipoOperacion tipoOperacion;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @NotNull
    @Column(name = "entidad_afectada", nullable = false, length = 100)
    private String entidadAfectada;

    @Column(name = "valor_anterior", columnDefinition = "JSON")
    private String valorAnterior;

    @Column(name = "valor_nuevo", columnDefinition = "JSON")
    private String valorNuevo;

    public enum TipoOperacion { INSERT, UPDATE, DELETE }

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) fechaHora = LocalDateTime.now();
    }
}
