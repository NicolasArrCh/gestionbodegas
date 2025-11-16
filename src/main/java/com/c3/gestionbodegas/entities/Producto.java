package com.c3.gestionbodegas.entities;

import java.math.BigDecimal;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "productos")
// @EntityListeners(AuditoriaListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Producto extends Auditable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable=false, length = 100)
    private String nombre;

    @Column(nullable= false, length = 100)
    private String categoria;
    
    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false, precision= 10, scale = 2)
    private BigDecimal precio;

    @ManyToOne
    @JoinColumn(name = "bodega_id", nullable = false)
    private Bodega bodega;
}
