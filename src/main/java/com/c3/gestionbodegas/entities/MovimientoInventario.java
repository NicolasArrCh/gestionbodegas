package com.c3.gestionbodegas.entities;

import java.time.LocalDateTime;

import com.c3.gestionbodegas.listeners.AuditoriaListener;
import com.c3.gestionbodegas.model.Auditable;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "movimientos_inventario")
@EntityListeners(AuditoriaListener.class) // ✅ ASEGURAR QUE ESTÁ ACTIVADO
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovimientoInventario extends Auditable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP", insertable = false, updatable = false)
    private LocalDateTime fecha;

    @NotNull(message = "Tipo es requerido")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    @NotNull(message = "Usuario es requerido")
    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "bodega_origen_id", nullable = true)
    private Bodega bodegaOrigen;

    @ManyToOne
    @JoinColumn(name = "bodega_destino_id", nullable = true)
    private Bodega bodegaDestino;

    // Alias para JSON
    @JsonProperty("usuario_id")
    private void setUsuarioId(Integer usuarioId) {
        if (usuarioId != null) {
            this.usuario = new Usuario();
            this.usuario.setId(usuarioId);
        }
    }

    @JsonProperty("bodega_origen_id")
    private void setBodegaOrigenId(Integer bodegaOrigenId) {
        if (bodegaOrigenId != null) {
            this.bodegaOrigen = new Bodega();
            this.bodegaOrigen.setId(bodegaOrigenId);
        }
    }

    @JsonProperty("bodega_destino_id")
    private void setBodegaDestinoId(Integer bodegaDestinoId) {
        if (bodegaDestinoId != null) {
            this.bodegaDestino = new Bodega();
            this.bodegaDestino.setId(bodegaDestinoId);
        }
    }

    public enum TipoMovimiento {
        ENTRADA,
        SALIDA,
        TRANSFERENCIA
    }
}