package com.c3.gestionbodegas.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.c3.gestionbodegas.entities.Bodega;

@Repository
public interface BodegaRepository extends JpaRepository <Bodega, Integer> {

    // Buscar por nombre exacto
    Bodega findByNombre(String nombre);

    // Verificar si ya existe una bodega con ese nombre
    boolean existsByNombre(String nombre);

    // Buscar bodegas por ubicación (útil si se quieren listar bodegas por ciudad)
    List<Bodega> findByUbicacionContainingIgnoreCase(String ubicacion);

    // Buscar bodegas con capacidad menor a cierto valor (por ejemplo, bodegas casi llenas)
    List<Bodega> findByCapacidadLessThan(Integer capacidad);

    // Consulta personalizada para obtener resumen general de stock por bodega (opcional para reportes)
    @Query("""
            SELECT b.nombre AS nombreBodega, SUM(p.stock) AS stockTotal
            FROM Bodega b
            JOIN Producto p ON p.bodega.id = b.id
            GROUP BY b.nombre
            """)
    List<Object[]> obtenerResumenStockPorBodega();
}
