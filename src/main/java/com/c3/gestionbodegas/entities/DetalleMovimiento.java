package com.c3.gestionbodegas.entities;

import com.c3.gestionbodegas.listeners.AuditoriaListener;
import com.c3.gestionbodegas.model.Auditable;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@Table(name = "detalle_movimiento")
@EntityListeners(AuditoriaListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleMovimiento extends Auditable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Movimiento es requerido")
    @ManyToOne(optional = false)
    @JoinColumn(name = "movimiento_id", nullable = false)
    private MovimientoInventario movimiento;

    @NotNull(message = "Producto es requerido")
    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @NotNull(message = "Cantidad es requerida")
    @Column(nullable = false)
    private Integer cantidad;

    // Alias para JSON: permite recibir tanto "movimiento_id" como "movimientoId"
    @JsonProperty("movimiento_id")
    private void setMovimientoId(Integer movimientoId) {
        if (movimientoId != null) {
            this.movimiento = new MovimientoInventario();
            this.movimiento.setId(movimientoId);
        }
    }

    // Alias para JSON: permite recibir tanto "producto_id" como "productoId"
    @JsonProperty("producto_id")
    private void setProductoId(Integer productoId) {
        if (productoId != null) {
            this.producto = new Producto();
            this.producto.setId(productoId);
        }
    }
}
